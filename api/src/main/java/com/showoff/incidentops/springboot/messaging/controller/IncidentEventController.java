package com.showoff.incidentops.springboot.messaging.controller;

import com.showoff.incidentops.springboot.messaging.dto.IncidentCreatedEvent;
import com.showoff.incidentops.springboot.messaging.dto.PublishIncidentEventRequest;
import com.showoff.incidentops.springboot.messaging.service.IncidentEventPublisher;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v5/events")
public class IncidentEventController {
    private final IncidentEventPublisher publisher;

    public IncidentEventController(IncidentEventPublisher publisher) {
        this.publisher = publisher;
    }

    @PostMapping("/incidents/created")
    public ResponseEntity<IncidentCreatedEvent> publishIncidentCreated(
        @Valid @RequestBody PublishIncidentEventRequest request
    ) {
        IncidentCreatedEvent event = IncidentCreatedEvent.fromRequest(request);
        publisher.publishIncidentCreated(event);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(event);
    }
}
