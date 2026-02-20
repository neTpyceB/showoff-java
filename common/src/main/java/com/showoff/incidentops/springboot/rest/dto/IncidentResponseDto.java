package com.showoff.incidentops.springboot.rest.dto;

public record IncidentResponseDto(String incidentId, String serviceId, int severity, String summary, String status) {}
