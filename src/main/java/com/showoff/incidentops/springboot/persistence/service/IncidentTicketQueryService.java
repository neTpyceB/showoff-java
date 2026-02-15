package com.showoff.incidentops.springboot.persistence.service;

import com.showoff.incidentops.springboot.persistence.dto.IncidentTicketResponse;
import org.springframework.data.domain.Page;

public interface IncidentTicketQueryService {
    IncidentTicketResponse getByTicketId(String ticketId);

    Page<IncidentTicketResponse> listByStatus(String status, int page, int size);

    Page<IncidentTicketResponse> searchByServiceAndMinSeverity(String serviceId, int minSeverity, int page, int size);
}
