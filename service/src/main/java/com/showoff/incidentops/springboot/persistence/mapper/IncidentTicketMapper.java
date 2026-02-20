package com.showoff.incidentops.springboot.persistence.mapper;

import com.showoff.incidentops.springboot.persistence.dto.CreateIncidentTicketRequest;
import com.showoff.incidentops.springboot.persistence.dto.IncidentTicketResponse;
import com.showoff.incidentops.springboot.persistence.entity.IncidentTicketEntity;
import org.springframework.stereotype.Component;

@Component
public class IncidentTicketMapper {
    public IncidentTicketEntity toNewEntity(String ticketId, CreateIncidentTicketRequest request) {
        validateNonBlank(ticketId, "ticketId");
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        return new IncidentTicketEntity(
            ticketId.trim().toUpperCase(),
            request.serviceId().trim().toLowerCase(),
            request.severity(),
            request.summary().trim(),
            "OPEN"
        );
    }

    public IncidentTicketResponse toResponse(IncidentTicketEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity must not be null");
        }
        return new IncidentTicketResponse(
            entity.getTicketId(),
            entity.getServiceId(),
            entity.getSeverity(),
            entity.getSummary(),
            entity.getStatus()
        );
    }

    private static void validateNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}
