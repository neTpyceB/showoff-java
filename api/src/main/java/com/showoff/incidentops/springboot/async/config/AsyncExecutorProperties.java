package com.showoff.incidentops.springboot.async.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "incidentops.async.executor")
public record AsyncExecutorProperties(
    int corePoolSize,
    int maxPoolSize,
    int queueCapacity,
    String threadNamePrefix
) {
    public AsyncExecutorProperties {
        if (corePoolSize < 1) {
            throw new IllegalArgumentException("corePoolSize must be at least 1");
        }
        if (maxPoolSize < corePoolSize) {
            throw new IllegalArgumentException("maxPoolSize must be greater than or equal to corePoolSize");
        }
        if (queueCapacity < 0) {
            throw new IllegalArgumentException("queueCapacity must be zero or greater");
        }
        if (threadNamePrefix == null || threadNamePrefix.isBlank()) {
            throw new IllegalArgumentException("threadNamePrefix must not be blank");
        }
    }
}
