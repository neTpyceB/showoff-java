package com.showoff.incidentops.springboot.persistence.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class IncidentTicketEntityTest {
    @Test
    void constructorAndSettersExposeMutableStateForJpa() {
        IncidentTicketEntity entity = new IncidentTicketEntity("TKT-7001", "payments-api", 4, "queue delay", "OPEN");
        assertNull(entity.getId());
        assertEquals("TKT-7001", entity.getTicketId());
        assertEquals("payments-api", entity.getServiceId());
        assertEquals(4, entity.getSeverity());
        assertEquals("queue delay", entity.getSummary());
        assertEquals("OPEN", entity.getStatus());

        entity.setTicketId("TKT-7002");
        entity.setServiceId("identity-api");
        entity.setSeverity(2);
        entity.setSummary("token issue");
        entity.setStatus("RESOLVED");

        assertEquals("TKT-7002", entity.getTicketId());
        assertEquals("identity-api", entity.getServiceId());
        assertEquals(2, entity.getSeverity());
        assertEquals("token issue", entity.getSummary());
        assertEquals("RESOLVED", entity.getStatus());
    }
}
