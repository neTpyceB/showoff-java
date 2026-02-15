package com.showoff.incidentops.springboot.persistence.service;

import com.showoff.incidentops.springboot.persistence.dto.CreateIncidentTicketRequest;
import com.showoff.incidentops.springboot.persistence.dto.IncidentTicketResponse;
import com.showoff.incidentops.springboot.persistence.entity.IncidentTicketEntity;
import com.showoff.incidentops.springboot.persistence.exception.IncidentTicketNotFoundException;
import com.showoff.incidentops.springboot.persistence.repository.IncidentTicketRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IncidentTicketServiceTest {
    @Test
    void create_mapsAndPersistsTicket() {
        IncidentTicketRepository repository = mock(IncidentTicketRepository.class);
        when(repository.save(any(IncidentTicketEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        IncidentTicketService service = new IncidentTicketService(repository);

        IncidentTicketResponse response = service.create(new CreateIncidentTicketRequest(" Payments-Api ", 4, " queue delay "));
        assertTrue(response.ticketId().startsWith("TKT-"));
        assertEquals("payments-api", response.serviceId());
        assertEquals(4, response.severity());
        assertEquals("queue delay", response.summary());
        assertEquals("OPEN", response.status());
    }

    @Test
    void getByTicketId_returnsMappedResponseOrThrows() {
        IncidentTicketRepository repository = mock(IncidentTicketRepository.class);
        when(repository.findByTicketId(eq("TKT-9001"))).thenReturn(
            Optional.of(new IncidentTicketEntity("TKT-9001", "identity-api", 3, "token issue", "OPEN"))
        );
        when(repository.findByTicketId(eq("TKT-9999"))).thenReturn(Optional.empty());
        IncidentTicketService service = new IncidentTicketService(repository);

        IncidentTicketResponse response = service.getByTicketId("tkt-9001");
        assertEquals("TKT-9001", response.ticketId());
        assertEquals("identity-api", response.serviceId());

        assertThrows(IncidentTicketNotFoundException.class, () -> service.getByTicketId("TKT-9999"));
    }

    @Test
    void service_validatesInput() {
        IncidentTicketRepository repository = mock(IncidentTicketRepository.class);
        IncidentTicketService service = new IncidentTicketService(repository);

        assertThrows(IllegalArgumentException.class, () -> service.create(null));
        assertThrows(IllegalArgumentException.class, () -> service.getByTicketId(null));
        assertThrows(IllegalArgumentException.class, () -> service.getByTicketId(" "));
        assertThrows(IllegalArgumentException.class, () -> service.listByStatus(null, 0, 20));
        assertThrows(IllegalArgumentException.class, () -> service.listByStatus(" ", 0, 20));
        assertThrows(IllegalArgumentException.class, () -> service.listByStatus("OPEN", -1, 20));
        assertThrows(IllegalArgumentException.class, () -> service.listByStatus("OPEN", 0, 0));
        assertThrows(IllegalArgumentException.class, () -> service.searchByServiceAndMinSeverity(null, 1, 0, 20));
        assertThrows(IllegalArgumentException.class, () -> service.searchByServiceAndMinSeverity(" ", 1, 0, 20));
        assertThrows(IllegalArgumentException.class, () -> service.searchByServiceAndMinSeverity("payments-api", 0, 0, 20));
        assertThrows(IllegalArgumentException.class, () -> service.searchByServiceAndMinSeverity("payments-api", 6, 0, 20));
        assertThrows(IllegalArgumentException.class, () -> service.searchByServiceAndMinSeverity("payments-api", 2, -1, 20));
        assertThrows(IllegalArgumentException.class, () -> service.searchByServiceAndMinSeverity("payments-api", 2, 0, 101));
        assertThrows(IllegalArgumentException.class, () -> service.createAndFailForRollback(null));
    }

    @Test
    void listByStatus_andSearchByServiceAndMinSeverity_mapPagedResults() {
        IncidentTicketRepository repository = mock(IncidentTicketRepository.class);
        IncidentTicketService service = new IncidentTicketService(repository);

        when(repository.findByStatusOrderBySeverityDescTicketIdAsc(eq("OPEN"), any())).thenReturn(
            new PageImpl<>(
                List.of(
                    new IncidentTicketEntity("TKT-7005", "payments-api", 5, "db outage", "OPEN"),
                    new IncidentTicketEntity("TKT-7004", "identity-api", 4, "token issue", "OPEN")
                ),
                PageRequest.of(0, 2),
                3
            )
        );
        when(repository.findByServiceAndMinSeverity(eq("Payments-Api"), eq(4), any())).thenReturn(
            new PageImpl<>(
                List.of(new IncidentTicketEntity("TKT-7005", "payments-api", 5, "db outage", "OPEN")),
                PageRequest.of(0, 1),
                1
            )
        );

        var byStatus = service.listByStatus(" open ", 0, 2);
        assertEquals(2, byStatus.getContent().size());
        assertEquals("TKT-7005", byStatus.getContent().get(0).ticketId());
        assertEquals(3, byStatus.getTotalElements());

        var search = service.searchByServiceAndMinSeverity(" Payments-Api ", 4, 0, 1);
        assertEquals(1, search.getContent().size());
        assertEquals("payments-api", search.getContent().get(0).serviceId());

        verify(repository).findByStatusOrderBySeverityDescTicketIdAsc(eq("OPEN"), any());
        verify(repository).findByServiceAndMinSeverity(eq("Payments-Api"), eq(4), any());
    }

    @Test
    void createAndFailForRollback_throwsAfterPersistAttempt() {
        IncidentTicketRepository repository = mock(IncidentTicketRepository.class);
        when(repository.save(any(IncidentTicketEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        IncidentTicketService service = new IncidentTicketService(repository);

        assertThrows(
            IllegalStateException.class,
            () -> service.createAndFailForRollback(new CreateIncidentTicketRequest("payments-api", 4, "db outage"))
        );
        verify(repository).save(any(IncidentTicketEntity.class));
    }
}
