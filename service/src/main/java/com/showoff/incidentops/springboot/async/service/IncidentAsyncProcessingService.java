package com.showoff.incidentops.springboot.async.service;

import com.showoff.incidentops.springboot.async.dto.ImpactScoreResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IncidentAsyncProcessingService {
    void dispatchAuditAsync(String incidentId, String requestedBy, String reason);

    CompletableFuture<ImpactScoreResponse> calculateImpactScoreAsync(String incidentId, int severity);

    List<AuditDispatchRecord> dispatchedAudits();
}
