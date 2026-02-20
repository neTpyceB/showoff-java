package com.showoff.incidentops.springboot.async.service;

import com.showoff.incidentops.springboot.async.dto.ImpactScoreResponse;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class DefaultIncidentAsyncProcessingService implements IncidentAsyncProcessingService {
    private final List<AuditDispatchRecord> auditDispatchLog = new CopyOnWriteArrayList<>();

    @Override
    @Async("incidentOpsAsyncExecutor")
    public void dispatchAuditAsync(String incidentId, String requestedBy, String reason) {
        String normalizedIncidentId = normalizeRequired(incidentId, "incidentId").toUpperCase();
        String normalizedRequestedBy = normalizeRequired(requestedBy, "requestedBy");
        String normalizedReason = normalizeRequired(reason, "reason");
        auditDispatchLog.add(new AuditDispatchRecord(
            normalizedIncidentId,
            normalizedRequestedBy,
            normalizedReason,
            Instant.now().toString(),
            Thread.currentThread().getName()
        ));
    }

    @Override
    @Async("incidentOpsAsyncExecutor")
    public CompletableFuture<ImpactScoreResponse> calculateImpactScoreAsync(String incidentId, int severity) {
        String normalizedIncidentId = normalizeRequired(incidentId, "incidentId").toUpperCase();
        if (severity < 1 || severity > 5) {
            throw new IllegalArgumentException("severity must be between 1 and 5");
        }
        int impactScore = (severity * 20) + Math.abs(normalizedIncidentId.hashCode() % 17);
        ImpactScoreResponse response = new ImpactScoreResponse(
            normalizedIncidentId,
            severity,
            impactScore,
            Instant.now().toString(),
            Thread.currentThread().getName()
        );
        return CompletableFuture.completedFuture(response);
    }

    @Override
    public List<AuditDispatchRecord> dispatchedAudits() {
        return List.copyOf(auditDispatchLog);
    }

    private static String normalizeRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }
}
