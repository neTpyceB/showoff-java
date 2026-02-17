package com.showoff.incidentops.springboot.messaging.service;

import com.showoff.incidentops.springboot.messaging.dto.IncidentCreatedEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IncidentEventConsumerTest {
    @Test
    void consumer_receivesAndStoresEvents() {
        IncidentEventConsumer consumer = new IncidentEventConsumer();
        IncidentCreatedEvent event = new IncidentCreatedEvent("INC-1", "payments-api", 4, "2026-02-17T10:00:00Z");

        consumer.onIncidentCreated(event);
        assertEquals(1, consumer.snapshot().size());
        assertEquals("INC-1", consumer.snapshot().get(0).incidentId());

        consumer.clear();
        assertEquals(0, consumer.snapshot().size());
    }

    @Test
    void consumer_validatesInput() {
        IncidentEventConsumer consumer = new IncidentEventConsumer();
        assertThrows(IllegalArgumentException.class, () -> consumer.onIncidentCreated(null));
    }
}
