package com.showoff.incidentops.springboot.async.service;

public record AuditDispatchRecord(
    String incidentId,
    String requestedBy,
    String reason,
    String dispatchedAt,
    String dispatchedByThread
) {}
