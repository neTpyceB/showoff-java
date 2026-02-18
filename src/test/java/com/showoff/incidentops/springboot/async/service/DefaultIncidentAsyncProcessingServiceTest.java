package com.showoff.incidentops.springboot.async.service;

import com.showoff.incidentops.springboot.async.dto.ImpactScoreResponse;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultIncidentAsyncProcessingServiceTest {
    @Test
    void dispatchAuditAndCalculateImpact_normalizeAndStoreData() {
        DefaultIncidentAsyncProcessingService service = new DefaultIncidentAsyncProcessingService();

        service.dispatchAuditAsync(" inc-9012 ", " ops-user ", " postmortem review ");
        assertEquals(1, service.dispatchedAudits().size());
        AuditDispatchRecord audit = service.dispatchedAudits().getFirst();
        assertEquals("INC-9012", audit.incidentId());
        assertEquals("ops-user", audit.requestedBy());
        assertEquals("postmortem review", audit.reason());
        assertTrue(!audit.dispatchedByThread().isBlank());

        CompletableFuture<ImpactScoreResponse> future = service.calculateImpactScoreAsync(" inc-9012 ", 4);
        ImpactScoreResponse response = future.join();
        assertEquals("INC-9012", response.incidentId());
        assertEquals(4, response.severity());
        assertTrue(response.impactScore() >= 80);
    }

    @Test
    void serviceValidatesInput() {
        DefaultIncidentAsyncProcessingService service = new DefaultIncidentAsyncProcessingService();

        assertThrows(IllegalArgumentException.class, () -> service.dispatchAuditAsync(null, "u", "r"));
        assertThrows(IllegalArgumentException.class, () -> service.dispatchAuditAsync(" ", "u", "r"));
        assertThrows(IllegalArgumentException.class, () -> service.dispatchAuditAsync("INC-1", null, "r"));
        assertThrows(IllegalArgumentException.class, () -> service.dispatchAuditAsync("INC-1", " ", "r"));
        assertThrows(IllegalArgumentException.class, () -> service.dispatchAuditAsync("INC-1", "u", null));
        assertThrows(IllegalArgumentException.class, () -> service.dispatchAuditAsync("INC-1", "u", " "));

        assertThrows(IllegalArgumentException.class, () -> service.calculateImpactScoreAsync(null, 3));
        assertThrows(IllegalArgumentException.class, () -> service.calculateImpactScoreAsync(" ", 3));
        assertThrows(IllegalArgumentException.class, () -> service.calculateImpactScoreAsync("INC-1", 0));
        assertThrows(IllegalArgumentException.class, () -> service.calculateImpactScoreAsync("INC-1", 6));
    }
}
