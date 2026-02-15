package com.showoff.incidentops.springboot.persistence.service;

import com.showoff.incidentops.springboot.persistence.dto.CreateIncidentTicketRequest;
import com.showoff.incidentops.springboot.persistence.dto.IncidentTicketResponse;

public interface IncidentTicketCommandService {
    IncidentTicketResponse create(CreateIncidentTicketRequest request);

    void createAndFailForRollback(CreateIncidentTicketRequest request);
}
