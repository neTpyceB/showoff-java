package com.showoff.incidentops.springboot.persistence.service;

import com.showoff.incidentops.springboot.config.IncidentOpsProperties;
import com.showoff.incidentops.springboot.persistence.dto.CreateIncidentTicketRequest;
import com.showoff.incidentops.springboot.persistence.dto.IncidentTicketResponse;
import com.showoff.incidentops.springboot.persistence.entity.IncidentTicketEntity;
import com.showoff.incidentops.springboot.persistence.exception.IncidentTicketNotFoundException;
import com.showoff.incidentops.springboot.persistence.mapper.IncidentTicketMapper;
import com.showoff.incidentops.springboot.persistence.repository.IncidentTicketRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class IncidentTicketService implements IncidentTicketCommandService, IncidentTicketQueryService {
    static final String CACHE_TICKET_BY_ID = "ticketsById";
    static final String CACHE_STATUS_PAGES = "ticketPagesByStatus";
    static final String CACHE_SERVICE_SEARCH_PAGES = "ticketPagesByServiceSeverity";

    private final IncidentTicketRepository repository;
    private final IncidentTicketMapper mapper;
    private final IncidentOpsProperties properties;
    private final AtomicInteger sequence = new AtomicInteger(5000);

    public IncidentTicketService(
        IncidentTicketRepository repository,
        IncidentTicketMapper mapper,
        IncidentOpsProperties properties
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.properties = properties;
    }

    @Override
    @Transactional
    @Caching(
        put = {@CachePut(cacheNames = CACHE_TICKET_BY_ID, key = "#result.ticketId()")},
        evict = {
            @CacheEvict(cacheNames = CACHE_STATUS_PAGES, allEntries = true),
            @CacheEvict(cacheNames = CACHE_SERVICE_SEARCH_PAGES, allEntries = true)
        }
    )
    public IncidentTicketResponse create(CreateIncidentTicketRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        String ticketId = "TKT-" + sequence.incrementAndGet();
        return mapper.toResponse(repository.save(mapper.toNewEntity(ticketId, request)));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CACHE_TICKET_BY_ID, key = "#p0.trim().toUpperCase()")
    public IncidentTicketResponse getByTicketId(String ticketId) {
        validateTicketId(ticketId);
        IncidentTicketEntity entity = repository.findByTicketId(ticketId.trim().toUpperCase())
            .orElseThrow(() -> new IncidentTicketNotFoundException(
                "ticket not found: " + ticketId.trim().toUpperCase()
            ));
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CACHE_STATUS_PAGES, key = "#root.target.statusPageKey(#p0, #p1, #p2)")
    public Page<IncidentTicketResponse> listByStatus(String status, int page, int size) {
        String effectiveStatus = normalizeStatusOrDefault(status);
        validateNonBlank(effectiveStatus, "status");
        validatePage(page, size);
        return repository.findByStatusOrderBySeverityDescTicketIdAsc(
            effectiveStatus.trim().toUpperCase(),
            PageRequest.of(page, size)
        ).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CACHE_SERVICE_SEARCH_PAGES, key = "#root.target.serviceSearchKey(#p0, #p1, #p2, #p3)")
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
        ).map(mapper::toResponse);
    }

    @Override
    @Transactional
    @Caching(
        put = {@CachePut(cacheNames = CACHE_TICKET_BY_ID, key = "#result.ticketId()")},
        evict = {
            @CacheEvict(cacheNames = CACHE_STATUS_PAGES, allEntries = true),
            @CacheEvict(cacheNames = CACHE_SERVICE_SEARCH_PAGES, allEntries = true)
        }
    )
    public IncidentTicketResponse updateStatus(String ticketId, String status) {
        validateTicketId(ticketId);
        validateNonBlank(status, "status");
        IncidentTicketEntity entity = repository.findByTicketId(ticketId.trim().toUpperCase())
            .orElseThrow(() -> new IncidentTicketNotFoundException(
                "ticket not found: " + ticketId.trim().toUpperCase()
            ));
        entity.setStatus(status.trim().toUpperCase());
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public void createAndFailForRollback(CreateIncidentTicketRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        create(request);
        throw new IllegalStateException("simulated failure after create");
    }

    private static void validateTicketId(String ticketId) {
        if (ticketId == null || ticketId.isBlank()) {
            throw new IllegalArgumentException("ticketId must not be blank");
        }
    }

    private void validatePage(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be >= 0");
        }
        int maxPageSize = properties.tickets().maxPageSize();
        if (size <= 0 || size > maxPageSize) {
            throw new IllegalArgumentException("size must be between 1 and " + maxPageSize);
        }
    }

    private static void validateNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }

    private String normalizeStatusOrDefault(String status) {
        if (status == null || status.isBlank()) {
            return properties.tickets().defaultStatus();
        }
        return status;
    }

    String statusPageKey(String status, int page, int size) {
        String effectiveStatus = normalizeStatusOrDefault(status).trim().toUpperCase();
        return effectiveStatus + ":" + page + ":" + size;
    }

    String serviceSearchKey(String serviceId, int minSeverity, int page, int size) {
        String normalizedService = serviceId == null ? "null" : serviceId.trim().toLowerCase();
        return normalizedService + ":" + minSeverity + ":" + page + ":" + size;
    }
}
