package com.showoff.incidentops.springboot.persistence.service;

import com.showoff.incidentops.springboot.persistence.dto.CreateIncidentTicketRequest;
import com.showoff.incidentops.springboot.persistence.dto.IncidentTicketResponse;

public interface IncidentTicketCommandService {
    IncidentTicketResponse create(CreateIncidentTicketRequest request);

    IncidentTicketResponse updateStatus(String ticketId, String status);

    void createAndFailForRollback(CreateIncidentTicketRequest request);
}
