package com.showoff.incidentops.springboot.messaging.service;

import com.showoff.incidentops.springboot.messaging.dto.IncidentCreatedEvent;

public interface IncidentEventPublisher {
    void publishIncidentCreated(IncidentCreatedEvent event);
}
