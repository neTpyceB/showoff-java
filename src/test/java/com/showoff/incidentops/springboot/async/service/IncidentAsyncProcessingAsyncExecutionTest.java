package com.showoff.incidentops.springboot.async.service;

import com.showoff.incidentops.springboot.async.config.AsyncExecutionConfig;
import com.showoff.incidentops.springboot.async.config.AsyncExecutorProperties;
import com.showoff.incidentops.springboot.async.dto.ImpactScoreResponse;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(classes = {
    AsyncExecutionConfig.class,
    DefaultIncidentAsyncProcessingService.class,
    IncidentAsyncProcessingAsyncExecutionTest.TestConfig.class
})
class IncidentAsyncProcessingAsyncExecutionTest {
    @Configuration
    static class TestConfig {
        @Bean
        AsyncExecutorProperties asyncExecutorProperties() {
            return new AsyncExecutorProperties(1, 1, 20, "incidentops-async-test-");
        }
    }

    @org.springframework.beans.factory.annotation.Autowired
    private IncidentAsyncProcessingService asyncService;

    @Test
    void calculateImpactScore_runsOnAsyncExecutorThread() throws Exception {
        String callerThread = Thread.currentThread().getName();

        ImpactScoreResponse response = asyncService.calculateImpactScoreAsync("INC-1901", 4).get(2, TimeUnit.SECONDS);

        assertTrue(response.calculatedByThread().startsWith("incidentops-async-test-"));
        assertNotEquals(callerThread, response.calculatedByThread());
    }

    @Test
    void dispatchAudit_runsAsBackgroundTask() {
        asyncService.dispatchAuditAsync("INC-1902", "sre-oncall", "notify stakeholders");

        Instant deadline = Instant.now().plus(Duration.ofSeconds(2));
        while (asyncService.dispatchedAudits().isEmpty() && Instant.now().isBefore(deadline)) {
            Thread.onSpinWait();
        }

        List<AuditDispatchRecord> records = asyncService.dispatchedAudits();
        assertFalse(records.isEmpty());
        AuditDispatchRecord record = records.getLast();
        assertTrue(record.dispatchedByThread().startsWith("incidentops-async-test-"));
        assertTrue(record.incidentId().startsWith("INC-"));
    }
}
