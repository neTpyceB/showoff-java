package com.showoff.incidentops.springboot.messaging.service;

import com.showoff.incidentops.springboot.config.IncidentOpsProperties;
import com.showoff.incidentops.springboot.messaging.dto.IncidentCreatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class KafkaIncidentEventPublisherTest {
    @Test
    void publishIncidentCreated_sendsMessageToConfiguredTopic() {
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, IncidentCreatedEvent> template = mock(KafkaTemplate.class);
        IncidentOpsProperties properties = new IncidentOpsProperties(
            new IncidentOpsProperties.Tickets("OPEN", 100),
            new IncidentOpsProperties.Integrations(
                new IncidentOpsProperties.Integrations.Redis("localhost", 6379),
                new IncidentOpsProperties.Integrations.Rabbitmq("localhost", 5672)
            ),
            new IncidentOpsProperties.Security("key", "secret"),
            new IncidentOpsProperties.Messaging(
                new IncidentOpsProperties.Messaging.Kafka("incident-events-test", "incidentops-test")
            )
        );
        KafkaIncidentEventPublisher publisher = new KafkaIncidentEventPublisher(template, properties);
        IncidentCreatedEvent event = new IncidentCreatedEvent("INC-1", "payments-api", 4, "2026-02-17T10:00:00Z");

        publisher.publishIncidentCreated(event);

        verify(template).send(eq("incident-events-test"), eq("INC-1"), eq(event));
        assertThrows(IllegalArgumentException.class, () -> publisher.publishIncidentCreated(null));
    }
}
