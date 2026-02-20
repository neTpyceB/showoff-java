package com.showoff.incidentops.springboot;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

@Service
public class IncidentService {
    private final AtomicInteger sequence = new AtomicInteger(1000);
    private final Map<String, IncidentResponse> storage = new ConcurrentHashMap<>();

    public IncidentResponse getIncident(String incidentId) {
        validateNonBlank(incidentId, "incidentId");
        return storage.computeIfAbsent(
            incidentId.trim().toUpperCase(),
            id -> new IncidentResponse(id, "unknown-service", 3, "placeholder incident", "OPEN")
        );
    }

    public IncidentResponse createIncident(IncidentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        String incidentId = "INC-" + sequence.incrementAndGet();
        IncidentResponse response = new IncidentResponse(
            incidentId,
            request.serviceId().trim().toLowerCase(),
            request.severity(),
            request.summary().trim(),
            "OPEN"
        );
        storage.put(incidentId, response);
        return response;
    }

    private static void validateNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must be non-blank");
        }
    }
}
