package com.showoff.incidentops.springboot.observability.service;

import com.showoff.incidentops.springboot.observability.dto.IncidentProcessingResponse;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IncidentObservabilityServiceTest {
    @Test
    void processIncident_recordsCounterTimerAndTracingOnSuccess() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        ObservationRegistry observationRegistry = ObservationRegistry.create();
        CountingObservationHandler handler = new CountingObservationHandler();
        observationRegistry.observationConfig().observationHandler(handler);

        IncidentObservabilityService service = new IncidentObservabilityService(meterRegistry, observationRegistry);

        IncidentProcessingResponse response = service.processIncident(" inc-7301 ", 4, false);

        assertEquals("INC-7301", response.incidentId());
        assertEquals(4, response.severity());
        assertEquals("SUCCESS", response.status());
        assertTrue(response.durationMs() >= 0);

        assertEquals(1.0, meterRegistry.get("incidentops.incident.processed").counter().count());
        assertEquals(0.0, meterRegistry.get("incidentops.incident.failed").counter().count());
        assertEquals(1L, meterRegistry.get("incidentops.incident.processing").timer().count());

        assertEquals(1, handler.started);
        assertEquals(1, handler.stopped);
        assertEquals(0, handler.errorCount);
    }

    @Test
    void processIncident_recordsFailureMetricsAndTracing() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        ObservationRegistry observationRegistry = ObservationRegistry.create();
        CountingObservationHandler handler = new CountingObservationHandler();
        observationRegistry.observationConfig().observationHandler(handler);

        IncidentObservabilityService service = new IncidentObservabilityService(meterRegistry, observationRegistry);

        assertThrows(IllegalStateException.class, () -> service.processIncident("INC-7302", 5, true));

        assertEquals(0.0, meterRegistry.get("incidentops.incident.processed").counter().count());
        assertEquals(1.0, meterRegistry.get("incidentops.incident.failed").counter().count());
        assertEquals(1L, meterRegistry.get("incidentops.incident.processing").timer().count());

        assertEquals(1, handler.started);
        assertEquals(1, handler.stopped);
        assertEquals(1, handler.errorCount);
    }

    @Test
    void processIncident_validatesArguments() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        ObservationRegistry observationRegistry = ObservationRegistry.create();
        IncidentObservabilityService service = new IncidentObservabilityService(meterRegistry, observationRegistry);

        assertThrows(IllegalArgumentException.class, () -> service.processIncident(null, 3, false));
        assertThrows(IllegalArgumentException.class, () -> service.processIncident(" ", 3, false));
        assertThrows(IllegalArgumentException.class, () -> service.processIncident("INC-7303", 0, false));
        assertThrows(IllegalArgumentException.class, () -> service.processIncident("INC-7303", 6, false));

        assertEquals(0.0, meterRegistry.get("incidentops.incident.processed").counter().count());
        assertEquals(4.0, meterRegistry.get("incidentops.incident.failed").counter().count());
        assertEquals(4L, meterRegistry.get("incidentops.incident.processing").timer().count());
    }

    private static final class CountingObservationHandler implements ObservationHandler<Observation.Context> {
        private int started;
        private int stopped;
        private int errorCount;

        @Override
        public boolean supportsContext(Observation.Context context) {
            return true;
        }

        @Override
        public void onStart(Observation.Context context) {
            started++;
        }

        @Override
        public void onStop(Observation.Context context) {
            stopped++;
        }

        @Override
        public void onError(Observation.Context context) {
            errorCount++;
        }
    }
}
