package com.showoff.incidentops.springboot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class IncidentServiceTest {
    @Test
    void getIncident_returnsExistingOrPlaceholder() {
        IncidentService service = new IncidentService();
        IncidentResponse placeholder = service.getIncident(" inc-2001 ");
        assertEquals("INC-2001", placeholder.incidentId());
        assertEquals("unknown-service", placeholder.serviceId());
        assertEquals("OPEN", placeholder.status());

        IncidentResponse same = service.getIncident("INC-2001");
        assertEquals(placeholder, same);
    }

    @Test
    void createIncident_usesRequestDataAndStoresValue() {
        IncidentService service = new IncidentService();
        IncidentResponse created = service.createIncident(new IncidentRequest(" Payments-Api ", 4, " queue delay "));

        assertTrue(created.incidentId().startsWith("INC-"));
        assertEquals("payments-api", created.serviceId());
        assertEquals(4, created.severity());
        assertEquals("queue delay", created.summary());
        assertEquals("OPEN", created.status());

        IncidentResponse fetched = service.getIncident(created.incidentId());
        assertEquals(created, fetched);
    }

    @Test
    void service_validatesInput() {
        IncidentService service = new IncidentService();
        assertThrows(IllegalArgumentException.class, () -> service.getIncident(" "));
        assertThrows(IllegalArgumentException.class, () -> service.createIncident(null));
    }
}
