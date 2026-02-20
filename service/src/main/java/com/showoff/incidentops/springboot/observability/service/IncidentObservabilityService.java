package com.showoff.incidentops.springboot.observability.service;

import com.showoff.incidentops.springboot.observability.dto.IncidentProcessingResponse;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class IncidentObservabilityService {
    private static final Logger log = LoggerFactory.getLogger(IncidentObservabilityService.class);

    private final ObservationRegistry observationRegistry;
    private final Counter processedCounter;
    private final Counter failedCounter;
    private final Timer processingTimer;

    public IncidentObservabilityService(MeterRegistry meterRegistry, ObservationRegistry observationRegistry) {
        this.observationRegistry = observationRegistry;
        this.processedCounter = Counter.builder("incidentops.incident.processed")
            .description("Total successfully processed incidents")
            .register(meterRegistry);
        this.failedCounter = Counter.builder("incidentops.incident.failed")
            .description("Total incident processing failures")
            .register(meterRegistry);
        this.processingTimer = Timer.builder("incidentops.incident.processing")
            .description("Incident processing duration")
            .publishPercentileHistogram()
            .register(meterRegistry);
    }

    public IncidentProcessingResponse processIncident(String incidentId, int severity, boolean simulateFailure) {
        long startNanos = System.nanoTime();
        Observation observation = Observation.start("incident.processing.flow", observationRegistry)
            .lowCardinalityKeyValue("component", "incident-observability-service");

        try (Observation.Scope ignored = observation.openScope()) {
            String normalizedIncidentId = normalizeIncidentId(incidentId);
            validateSeverity(severity);

            if (simulateFailure) {
                throw new IllegalStateException("simulated downstream failure");
            }

            long durationNanos = System.nanoTime() - startNanos;
            processingTimer.record(durationNanos, TimeUnit.NANOSECONDS);
            processedCounter.increment();

            long durationMs = TimeUnit.NANOSECONDS.toMillis(durationNanos);
            log.info(
                "incident_processed incidentId={} severity={} durationMs={} outcome={}",
                normalizedIncidentId,
                severity,
                durationMs,
                "success"
            );

            observation.lowCardinalityKeyValue("outcome", "success");
            return new IncidentProcessingResponse(normalizedIncidentId, severity, "SUCCESS", durationMs);
        } catch (RuntimeException ex) {
            long durationNanos = System.nanoTime() - startNanos;
            processingTimer.record(durationNanos, TimeUnit.NANOSECONDS);
            failedCounter.increment();

            log.warn(
                "incident_processing_failed incidentId={} severity={} reason={} outcome={}",
                incidentId,
                severity,
                ex.getMessage(),
                "failure"
            );

            observation.lowCardinalityKeyValue("outcome", "failure");
            observation.error(ex);
            throw ex;
        } finally {
            observation.stop();
        }
    }

    private static String normalizeIncidentId(String incidentId) {
        if (incidentId == null || incidentId.isBlank()) {
            throw new IllegalArgumentException("incidentId must not be blank");
        }
        return incidentId.trim().toUpperCase();
    }

    private static void validateSeverity(int severity) {
        if (severity < 1 || severity > 5) {
            throw new IllegalArgumentException("severity must be between 1 and 5");
        }
    }
}
