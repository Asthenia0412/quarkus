package io.quarkus.observability.devresource;

import java.io.Closeable;
import java.time.Duration;

import io.quarkus.observability.common.config.ContainerConfig;
import io.quarkus.runtime.LaunchMode;

/**
 * Simple container abstraction, e.g. similar to GenericContainer
 */
public interface Container<T extends ContainerConfig> {
    void start();

    void stop();

    String getContainerId();

    void withStartupTimeout(Duration duration);

    /**
     * Apply Quarkus process ownership labels ({@code QUARKUS_PROCESS_UUID},
     * {@code QUARKUS_LAUNCH_MODE}) so {@code ContainerLocator} does not rediscover
     * this JVM's containers as shared during hot reload (issue #57067).
     * No-op by default; implementations wrapping a Testcontainers
     * {@code GenericContainer} should override and delegate to {@code ConfigureUtil}.
     */
    default void configureDevServicesLabels(LaunchMode launchMode) {
    }

    Closeable closeableCallback(String serviceName);
}
