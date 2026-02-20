package com.showoff.incidentops.springboot.rest.service;

import com.showoff.incidentops.springboot.rest.dto.CreateIncidentRequest;
import com.showoff.incidentops.springboot.rest.exception.IncidentNotFoundException;
import com.showoff.incidentops.springboot.rest.model.IncidentEntity;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class IncidentCommandService {
    private final AtomicInteger sequence = new AtomicInteger(2000);
    private final Map<String, IncidentEntity> storage = new ConcurrentHashMap<>();

    public IncidentEntity create(CreateIncidentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        String incidentId = "INC-" + sequence.incrementAndGet();
        IncidentEntity entity = new IncidentEntity(
            incidentId,
            request.serviceId().trim().toLowerCase(),
            request.severity(),
            request.summary().trim(),
            "OPEN"
        );
        storage.put(incidentId, entity);
        return entity;
    }

    public IncidentEntity getById(String incidentId) {
        validateIncidentId(incidentId);
        IncidentEntity entity = storage.get(incidentId.trim().toUpperCase());
        if (entity == null) {
            throw new IncidentNotFoundException("incident not found: " + incidentId.trim().toUpperCase());
        }
        return entity;
    }

    private static void validateIncidentId(String incidentId) {
        if (incidentId == null || incidentId.isBlank()) {
            throw new IllegalArgumentException("incidentId must not be blank");
        }
    }
}
