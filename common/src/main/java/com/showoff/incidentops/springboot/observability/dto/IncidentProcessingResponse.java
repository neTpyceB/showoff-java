package com.showoff.incidentops.springboot.observability.dto;

public record IncidentProcessingResponse(
    String incidentId,
    int severity,
    String status,
    long durationMs
) {}
