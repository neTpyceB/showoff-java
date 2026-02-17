package com.showoff.incidentops.springboot.messaging.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IncidentCreatedEventTest {
    @Test
    void fromRequest_mapsAndNormalizesFields() {
        IncidentCreatedEvent event = IncidentCreatedEvent.fromRequest(
            new PublishIncidentEventRequest(" inc-7001 ", " Payments-Api ", 4)
        );
        assertEquals("INC-7001", event.incidentId());
        assertEquals("payments-api", event.serviceId());
        assertEquals(4, event.severity());
        assertTrue(event.emittedAt().contains("T"));
    }

    @Test
    void event_validatesArguments() {
        assertThrows(IllegalArgumentException.class, () -> IncidentCreatedEvent.fromRequest(null));
        assertThrows(IllegalArgumentException.class, () -> new IncidentCreatedEvent(null, "payments-api", 4, "x"));
        assertThrows(IllegalArgumentException.class, () -> new IncidentCreatedEvent(" ", "payments-api", 4, "x"));
        assertThrows(IllegalArgumentException.class, () -> new IncidentCreatedEvent("INC-1", null, 4, "x"));
        assertThrows(IllegalArgumentException.class, () -> new IncidentCreatedEvent("INC-1", " ", 4, "x"));
        assertThrows(IllegalArgumentException.class, () -> new IncidentCreatedEvent("INC-1", "payments-api", 0, "x"));
        assertThrows(IllegalArgumentException.class, () -> new IncidentCreatedEvent("INC-1", "payments-api", 6, "x"));
        assertThrows(IllegalArgumentException.class, () -> new IncidentCreatedEvent("INC-1", "payments-api", 4, null));
        assertThrows(IllegalArgumentException.class, () -> new IncidentCreatedEvent("INC-1", "payments-api", 4, " "));
    }
}
