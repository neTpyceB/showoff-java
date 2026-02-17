package com.showoff.incidentops.springboot.messaging.service;

import com.showoff.incidentops.springboot.config.IncidentOpsProperties;
import com.showoff.incidentops.springboot.messaging.dto.IncidentCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaIncidentEventPublisher implements IncidentEventPublisher {
    private final KafkaTemplate<String, IncidentCreatedEvent> kafkaTemplate;
    private final IncidentOpsProperties properties;

    public KafkaIncidentEventPublisher(
        KafkaTemplate<String, IncidentCreatedEvent> kafkaTemplate,
        IncidentOpsProperties properties
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
    }

    @Override
    public void publishIncidentCreated(IncidentCreatedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null");
        }
        kafkaTemplate.send(properties.messaging().kafka().topic(), event.incidentId(), event);
    }
}
