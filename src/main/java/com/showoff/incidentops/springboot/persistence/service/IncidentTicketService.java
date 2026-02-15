package com.showoff.incidentops.springboot.persistence.service;

import com.showoff.incidentops.springboot.persistence.dto.CreateIncidentTicketRequest;
import com.showoff.incidentops.springboot.persistence.dto.IncidentTicketResponse;
import com.showoff.incidentops.springboot.persistence.entity.IncidentTicketEntity;
import com.showoff.incidentops.springboot.persistence.exception.IncidentTicketNotFoundException;
import com.showoff.incidentops.springboot.persistence.repository.IncidentTicketRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class IncidentTicketService {
    private final IncidentTicketRepository repository;
    private final AtomicInteger sequence = new AtomicInteger(5000);

    public IncidentTicketService(IncidentTicketRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public IncidentTicketResponse create(CreateIncidentTicketRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        IncidentTicketEntity entity = new IncidentTicketEntity(
            "TKT-" + sequence.incrementAndGet(),
            request.serviceId().trim().toLowerCase(),
            request.severity(),
            request.summary().trim(),
            "OPEN"
        );
        IncidentTicketEntity saved = repository.save(entity);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public IncidentTicketResponse getByTicketId(String ticketId) {
        validateTicketId(ticketId);
        IncidentTicketEntity entity = repository.findByTicketId(ticketId.trim().toUpperCase())
            .orElseThrow(() -> new IncidentTicketNotFoundException(
                "ticket not found: " + ticketId.trim().toUpperCase()
            ));
        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public Page<IncidentTicketResponse> listByStatus(String status, int page, int size) {
        validateNonBlank(status, "status");
        validatePage(page, size);
        return repository.findByStatusOrderBySeverityDescTicketIdAsc(
            status.trim().toUpperCase(),
            PageRequest.of(page, size)
        ).map(IncidentTicketService::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<IncidentTicketResponse> searchByServiceAndMinSeverity(
        String serviceId,
        int minSeverity,
        int page,
        int size
    ) {
        validateNonBlank(serviceId, "serviceId");
        if (minSeverity < 1 || minSeverity > 5) {
            throw new IllegalArgumentException("minSeverity must be between 1 and 5");
        }
        validatePage(page, size);
        return repository.findByServiceAndMinSeverity(
            serviceId.trim(),
            minSeverity,
            PageRequest.of(page, size)
        ).map(IncidentTicketService::toResponse);
    }

    @Transactional
    public void createAndFailForRollback(CreateIncidentTicketRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        create(request);
        throw new IllegalStateException("simulated failure after create");
    }

    private static IncidentTicketResponse toResponse(IncidentTicketEntity entity) {
        return new IncidentTicketResponse(
            entity.getTicketId(),
            entity.getServiceId(),
            entity.getSeverity(),
            entity.getSummary(),
            entity.getStatus()
        );
    }

    private static void validateTicketId(String ticketId) {
        if (ticketId == null || ticketId.isBlank()) {
            throw new IllegalArgumentException("ticketId must not be blank");
        }
    }

    private static void validatePage(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be >= 0");
        }
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("size must be between 1 and 100");
        }
    }

    private static void validateNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}
