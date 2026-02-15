package com.showoff.incidentops.springboot;

public record IncidentResponse(String incidentId, String serviceId, int severity, String summary, String status) {}
