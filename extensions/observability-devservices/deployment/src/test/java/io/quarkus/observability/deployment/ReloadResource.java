package io.quarkus.observability.deployment;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Minimal endpoint used by ObservabilityDevServicesDevModeTestCase to
 * trigger and verify dev-mode hot reload across observability dev services.
 */
@Path("/hello")
public class ReloadResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "hello-observability";
    }
}
