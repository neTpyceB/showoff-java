package com.showoff.incidentops.springboot.persistence.controller;

import com.showoff.incidentops.springboot.persistence.dto.CreateIncidentTicketRequest;
import com.showoff.incidentops.springboot.persistence.dto.IncidentTicketResponse;
import com.showoff.incidentops.springboot.persistence.exception.IncidentTicketNotFoundException;
import com.showoff.incidentops.springboot.persistence.service.IncidentTicketService;
import com.showoff.incidentops.springboot.rest.exception.ApiErrorResponse;
import com.showoff.incidentops.springboot.rest.exception.GlobalApiExceptionHandler;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class IncidentTicketControllerTest {
    private IncidentTicketService ticketService;
    private MockMvc mvc;
    private LocalValidatorFactoryBean validator;

    @BeforeEach
    void setUp() {
        ticketService = mock(IncidentTicketService.class);
        IncidentTicketController controller = new IncidentTicketController(ticketService);
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalApiExceptionHandler())
            .setValidator(validator)
            .build();
    }

    @AfterEach
    void tearDown() {
        validator.close();
    }

    @Test
    void create_returns201WithJsonResponse() throws Exception {
        when(ticketService.create(any(CreateIncidentTicketRequest.class))).thenReturn(
            new IncidentTicketResponse("TKT-7001", "payments-api", 4, "queue delay", "OPEN")
        );

        mvc.perform(post("/api/v4/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "serviceId": "payments-api",
                      "severity": 4,
                      "summary": "queue delay"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.ticketId").value("TKT-7001"))
            .andExpect(jsonPath("$.serviceId").value("payments-api"));
    }

    @Test
    void getById_returns200ForExistingTicket() throws Exception {
        when(ticketService.getByTicketId(eq("TKT-7001"))).thenReturn(
            new IncidentTicketResponse("TKT-7001", "identity-api", 3, "token issue", "OPEN")
        );

        mvc.perform(get("/api/v4/tickets/TKT-7001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ticketId").value("TKT-7001"))
            .andExpect(jsonPath("$.serviceId").value("identity-api"));
    }

    @Test
    void listAndSearch_returnPagedPayloads() throws Exception {
        when(ticketService.listByStatus(eq("OPEN"), eq(0), eq(2))).thenReturn(
            new PageImpl<>(
                List.of(
                    new IncidentTicketResponse("TKT-7002", "payments-api", 5, "db outage", "OPEN"),
                    new IncidentTicketResponse("TKT-7001", "identity-api", 4, "queue delay", "OPEN")
                ),
                PageRequest.of(0, 2),
                3
            )
        );
        when(ticketService.searchByServiceAndMinSeverity(eq("payments-api"), eq(4), eq(0), eq(1))).thenReturn(
            new PageImpl<>(
                List.of(new IncidentTicketResponse("TKT-7002", "payments-api", 5, "db outage", "OPEN")),
                PageRequest.of(0, 1),
                1
            )
        );

        mvc.perform(get("/api/v4/tickets")
                .param("status", "OPEN")
                .param("page", "0")
                .param("size", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].ticketId").value("TKT-7002"))
            .andExpect(jsonPath("$.size").value(2))
            .andExpect(jsonPath("$.totalElements").value(3));

        mvc.perform(get("/api/v4/tickets/search")
                .param("serviceId", "payments-api")
                .param("minSeverity", "4")
                .param("page", "0")
                .param("size", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].serviceId").value("payments-api"))
            .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void validationErrors_return400WithDetails() throws Exception {
        mvc.perform(post("/api/v4/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "serviceId": " ",
                      "severity": 0,
                      "summary": " "
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.details.serviceId").exists())
            .andExpect(jsonPath("$.details.severity").exists())
            .andExpect(jsonPath("$.details.summary").exists());
    }

    @Test
    void centralizedExceptionHandling_maps404And400And500() throws Exception {
        when(ticketService.getByTicketId(eq("TKT-8888"))).thenThrow(new IncidentTicketNotFoundException("ticket not found: TKT-8888"));
        when(ticketService.getByTicketId(eq("TKT-8889"))).thenThrow(new IllegalArgumentException("ticketId must not be blank"));
        when(ticketService.getByTicketId(eq("TKT-8890"))).thenThrow(new RuntimeException("unexpected"));

        mvc.perform(get("/api/v4/tickets/TKT-8888"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("TICKET_NOT_FOUND"));

        mvc.perform(get("/api/v4/tickets/TKT-8889"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("BAD_REQUEST"));

        mvc.perform(get("/api/v4/tickets/TKT-8890"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));
    }

    @Test
    void constraintViolationHandler_branchCovered() {
        GlobalApiExceptionHandler handler = new GlobalApiExceptionHandler();
        ResponseEntity<ApiErrorResponse> response = handler.handleConstraintViolation(
            new ConstraintViolationException("ticketId must match TKT-<digits>", java.util.Set.of())
        );
        assertEquals(400, response.getStatusCode().value());
        assertEquals("CONSTRAINT_VIOLATION", response.getBody().code());
    }
}
