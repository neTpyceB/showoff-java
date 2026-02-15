package com.showoff.incidentops.springboot.persistence.service;

import com.showoff.incidentops.springboot.persistence.dto.CreateIncidentTicketRequest;
import com.showoff.incidentops.springboot.persistence.repository.IncidentTicketRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(
    classes = com.showoff.incidentops.springboot.IncidentApiApplication.class,
    properties = {"spring.main.web-application-type=none", "spring.main.banner-mode=off"}
)
class IncidentTicketServiceTransactionIntegrationTest {
    @Autowired
    private IncidentTicketService service;

    @Autowired
    private IncidentTicketRepository repository;

    @Test
    void createAndFailForRollback_rollsBackAtomicWrite() {
        repository.deleteAll();
        assertThrows(
            IllegalStateException.class,
            () -> service.createAndFailForRollback(new CreateIncidentTicketRequest("payments-api", 5, "db outage"))
        );
        assertEquals(0, repository.count());
    }
}
