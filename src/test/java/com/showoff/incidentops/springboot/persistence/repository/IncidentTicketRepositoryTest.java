package com.showoff.incidentops.springboot.persistence.repository;

import com.showoff.incidentops.springboot.persistence.entity.IncidentTicketEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
    classes = com.showoff.incidentops.springboot.IncidentApiApplication.class,
    properties = {"spring.main.web-application-type=none", "spring.main.banner-mode=off"}
)
class IncidentTicketRepositoryTest {
    @Autowired
    private IncidentTicketRepository repository;

    @Test
    void findByTicketId_returnsSavedEntity() {
        IncidentTicketEntity saved = repository.save(
            new IncidentTicketEntity("TKT-7001", "payments-api", 4, "queue delay", "OPEN")
        );

        assertTrue(saved.getId() != null);
        IncidentTicketEntity found = repository.findByTicketId("TKT-7001").orElseThrow();
        assertEquals("payments-api", found.getServiceId());
        assertEquals(4, found.getSeverity());
        assertEquals("queue delay", found.getSummary());
        assertEquals("OPEN", found.getStatus());
    }

    @Test
    void pagingAndCustomQueries_returnExpectedSlices() {
        repository.save(new IncidentTicketEntity("TKT-7101", "payments-api", 5, "db outage", "OPEN"));
        repository.save(new IncidentTicketEntity("TKT-7102", "identity-api", 3, "token issue", "OPEN"));
        repository.save(new IncidentTicketEntity("TKT-7103", "payments-api", 4, "queue delay", "OPEN"));
        repository.save(new IncidentTicketEntity("TKT-7104", "payments-api", 2, "minor alert", "RESOLVED"));

        Page<IncidentTicketEntity> openPage = repository.findByStatusOrderBySeverityDescTicketIdAsc(
            "OPEN",
            PageRequest.of(0, 2)
        );
        assertEquals(2, openPage.getContent().size());
        assertEquals("TKT-7101", openPage.getContent().get(0).getTicketId());
        assertEquals(3, openPage.getTotalElements());

        Page<IncidentTicketEntity> searchPage = repository.findByServiceAndMinSeverity(
            "PAYMENTS-API",
            4,
            PageRequest.of(0, 10)
        );
        assertEquals(2, searchPage.getContent().size());
        assertEquals("TKT-7101", searchPage.getContent().get(0).getTicketId());
        assertEquals("TKT-7103", searchPage.getContent().get(1).getTicketId());
    }
}
