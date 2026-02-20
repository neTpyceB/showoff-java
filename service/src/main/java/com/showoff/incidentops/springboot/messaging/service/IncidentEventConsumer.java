package com.showoff.incidentops.springboot.messaging.service;

import com.showoff.incidentops.springboot.messaging.dto.IncidentCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class IncidentEventConsumer {
    private static final Logger log = LoggerFactory.getLogger(IncidentEventConsumer.class);

    private final CopyOnWriteArrayList<IncidentCreatedEvent> receivedEvents = new CopyOnWriteArrayList<>();

    @KafkaListener(
        topics = "${incidentops.messaging.kafka.topic}",
        groupId = "${incidentops.messaging.kafka.group-id}"
    )
    public void onIncidentCreated(IncidentCreatedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null");
        }
        receivedEvents.add(event);
        log.info("Consumed incident event: incidentId={} serviceId={}", event.incidentId(), event.serviceId());
    }

    public List<IncidentCreatedEvent> snapshot() {
        return new ArrayList<>(receivedEvents);
    }

    public void clear() {
        receivedEvents.clear();
    }
}
