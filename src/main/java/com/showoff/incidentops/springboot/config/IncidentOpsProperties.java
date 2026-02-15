package com.showoff.incidentops.springboot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "incidentops")
public record IncidentOpsProperties(
    Tickets tickets,
    Integrations integrations,
    Security security
) {
    public record Tickets(String defaultStatus, int maxPageSize) {}

    public record Integrations(Redis redis, Rabbitmq rabbitmq) {
        public record Redis(String host, int port) {}

        public record Rabbitmq(String host, int port) {}
    }

    public record Security(String apiKey, String signingSecret) {}
}
