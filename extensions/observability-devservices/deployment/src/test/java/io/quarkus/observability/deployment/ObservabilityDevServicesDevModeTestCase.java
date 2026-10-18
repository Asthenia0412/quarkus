package io.quarkus.observability.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Supplier;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusDevModeTest;
import io.quarkus.test.common.http.TestHTTPResource;
import io.restassured.RestAssured;

/**
 * Verifies that observability dev service containers survive a hot reload
 * (issue #57067). Before the ownership-label fix, {@code ContainerLocator}
 * mistook this JVM's container for an external shared service on reload and
 * either stopped it or failed to rediscover it afterwards.
 * <p>
 * Pattern follows {@code KafkaDevServicesDevModeTestCase}: a small
 * {@link QuarkusDevModeTest} that starts dev mode, modifies a source file
 * to trigger reload, and verifies the app still responds afterwards.
 */
@Tag("devmode")
@EnabledIfSystemProperty(named = "start-containers", matches = "true")
@Disabled("Manual test - requires Docker and long startup")
public class ObservabilityDevServicesDevModeTestCase {

    static final String APP_PROPERTIES = "quarkus.observability.enabled-in-tests=true\n" +
            "quarkus.observability.lgtm.timeout=PT3M\n";

    @RegisterExtension
    public static QuarkusDevModeTest test = new QuarkusDevModeTest()
            .setArchiveProducer(new Supplier<>() {
                @Override
                public JavaArchive get() {
                    return ShrinkWrap.create(JavaArchive.class)
                            .addClasses(ReloadResource.class)
                            .addAsResource(new StringAsset(APP_PROPERTIES),
                                    "application.properties");
                }
            });

    @TestHTTPResource("/hello")
    String helloUrl;

    @Test
    public void devServiceSurvivesHotReload() {
        // Initial poke: verifies dev service containers came up on startup
        assertThat(RestAssured.given().baseUri(helloUrl).when().get().getStatusCode())
                .isEqualTo(200);

        // Change source file → triggers Quarkus dev-mode reload
        test.modifySourceFile(ReloadResource.class,
                s -> s.replace("hello-observability", "hello-observability-reloaded"));

        // After reload, endpoint must respond again. If observability dev services
        // had been misidentified as external shared containers and torn down,
        // the app would not come back up.
        assertThat(RestAssured.given().baseUri(helloUrl).when().get().getStatusCode())
                .isEqualTo(200);
        assertThat(RestAssured.given().baseUri(helloUrl).when().get().asString())
                .isEqualTo("hello-observability-reloaded");
    }
}
