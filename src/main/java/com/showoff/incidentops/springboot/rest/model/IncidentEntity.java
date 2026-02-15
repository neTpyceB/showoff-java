package com.showoff.incidentops.springboot.rest.model;

public record IncidentEntity(String incidentId, String serviceId, int severity, String summary, String status) {}
