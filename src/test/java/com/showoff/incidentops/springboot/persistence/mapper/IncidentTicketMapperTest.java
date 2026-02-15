package com.showoff.incidentops.springboot.persistence.mapper;

import com.showoff.incidentops.springboot.persistence.dto.CreateIncidentTicketRequest;
import com.showoff.incidentops.springboot.persistence.dto.IncidentTicketResponse;
import com.showoff.incidentops.springboot.persistence.entity.IncidentTicketEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IncidentTicketMapperTest {
    @Test
    void toNewEntity_mapsRequestToNormalizedEntity() {
        IncidentTicketMapper mapper = new IncidentTicketMapper();
        IncidentTicketEntity entity = mapper.toNewEntity(
            " tkt-7001 ",
            new CreateIncidentTicketRequest(" Payments-Api ", 4, " queue delay ")
        );

        assertEquals("TKT-7001", entity.getTicketId());
        assertEquals("payments-api", entity.getServiceId());
        assertEquals(4, entity.getSeverity());
        assertEquals("queue delay", entity.getSummary());
        assertEquals("OPEN", entity.getStatus());
    }

    @Test
    void toResponse_mapsEntityToApiDto() {
        IncidentTicketMapper mapper = new IncidentTicketMapper();
        IncidentTicketResponse response = mapper.toResponse(
            new IncidentTicketEntity("TKT-7002", "identity-api", 3, "token issue", "OPEN")
        );
        assertEquals("TKT-7002", response.ticketId());
        assertEquals("identity-api", response.serviceId());
        assertEquals(3, response.severity());
        assertEquals("token issue", response.summary());
        assertEquals("OPEN", response.status());
    }

    @Test
    void mapper_validatesInput() {
        IncidentTicketMapper mapper = new IncidentTicketMapper();
        assertThrows(
            IllegalArgumentException.class,
            () -> mapper.toNewEntity(null, new CreateIncidentTicketRequest("payments-api", 3, "token issue"))
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> mapper.toNewEntity(" ", new CreateIncidentTicketRequest("payments-api", 3, "token issue"))
        );
        assertThrows(IllegalArgumentException.class, () -> mapper.toNewEntity("TKT-7003", null));
        assertThrows(IllegalArgumentException.class, () -> mapper.toResponse(null));
    }
}
