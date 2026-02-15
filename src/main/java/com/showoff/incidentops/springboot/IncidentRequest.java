package com.showoff.incidentops.springboot;

public record IncidentRequest(String serviceId, int severity, String summary) {
    public IncidentRequest {
        validateNonBlank(serviceId, "serviceId");
        validateNonBlank(summary, "summary");
        if (severity < 1 || severity > 5) {
            throw new IllegalArgumentException("severity must be 1..5");
        }
    }

    private static void validateNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must be non-blank");
        }
    }
}
