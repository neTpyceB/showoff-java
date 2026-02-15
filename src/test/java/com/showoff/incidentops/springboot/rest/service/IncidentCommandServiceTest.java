package com.showoff.incidentops.springboot.rest.service;

import com.showoff.incidentops.springboot.rest.dto.CreateIncidentRequest;
import com.showoff.incidentops.springboot.rest.exception.IncidentNotFoundException;
import com.showoff.incidentops.springboot.rest.model.IncidentEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IncidentCommandServiceTest {
    @Test
    void createAndGetById_workWithNormalizedData() {
        IncidentCommandService service = new IncidentCommandService();
        IncidentEntity created = service.create(new CreateIncidentRequest(" Payments-Api ", 4, " queue delay "));

        assertTrue(created.incidentId().startsWith("INC-"));
        assertEquals("payments-api", created.serviceId());
        assertEquals(4, created.severity());
        assertEquals("queue delay", created.summary());
        assertEquals("OPEN", created.status());

        IncidentEntity fetched = service.getById(created.incidentId().toLowerCase());
        assertEquals(created, fetched);
    }

    @Test
    void service_throwsForInvalidArgumentsAndMissingEntities() {
        IncidentCommandService service = new IncidentCommandService();
        assertThrows(IllegalArgumentException.class, () -> service.create(null));
        assertThrows(IllegalArgumentException.class, () -> service.getById(null));
        assertThrows(IllegalArgumentException.class, () -> service.getById(" "));
        assertThrows(IncidentNotFoundException.class, () -> service.getById("INC-9999"));
    }
}
