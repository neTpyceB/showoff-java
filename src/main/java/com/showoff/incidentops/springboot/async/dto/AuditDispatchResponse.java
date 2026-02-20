package com.showoff.incidentops.springboot.async.dto;

public record AuditDispatchResponse(
    String dispatchId,
    String incidentId,
    String status
) {}
