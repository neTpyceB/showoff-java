package com.showoff.incidentops.springboot.persistence.dto;

public record IncidentTicketResponse(
    String ticketId,
    String serviceId,
    int severity,
    String summary,
    String status
) {}
