package com.showoff.incidentops.springboot.messaging.dto;

import java.time.Instant;

public record IncidentCreatedEvent(
    String incidentId,
    String serviceId,
    int severity,
    String emittedAt
) {
    public IncidentCreatedEvent {
        validateNonBlank(incidentId, "incidentId");
        validateNonBlank(serviceId, "serviceId");
        validateNonBlank(emittedAt, "emittedAt");
        if (severity < 1 || severity > 5) {
            throw new IllegalArgumentException("severity must be between 1 and 5");
        }
    }

    public static IncidentCreatedEvent fromRequest(PublishIncidentEventRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        return new IncidentCreatedEvent(
            request.incidentId().trim().toUpperCase(),
            request.serviceId().trim().toLowerCase(),
            request.severity(),
            Instant.now().toString()
        );
    }

    private static void validateNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}
