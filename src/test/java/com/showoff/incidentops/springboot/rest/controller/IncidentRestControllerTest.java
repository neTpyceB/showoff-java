package com.showoff.incidentops.springboot.rest.controller;

import com.showoff.incidentops.springboot.rest.dto.CreateIncidentRequest;
import com.showoff.incidentops.springboot.rest.exception.GlobalApiExceptionHandler;
import com.showoff.incidentops.springboot.rest.exception.IncidentNotFoundException;
import com.showoff.incidentops.springboot.rest.model.IncidentEntity;
import com.showoff.incidentops.springboot.rest.service.IncidentCommandService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import jakarta.validation.ConstraintViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class IncidentRestControllerTest {
    private IncidentCommandService incidentService;
    private MockMvc mvc;
    private LocalValidatorFactoryBean validator;

    @BeforeEach
    void setUp() {
        incidentService = mock(IncidentCommandService.class);
        IncidentRestController controller = new IncidentRestController(incidentService);
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
        when(incidentService.create(any(CreateIncidentRequest.class))).thenReturn(
            new IncidentEntity("INC-3001", "payments-api", 4, "queue delay", "OPEN")
        );

        mvc.perform(post("/api/v2/incidents")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "serviceId": "payments-api",
                      "severity": 4,
                      "summary": "queue delay"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.incidentId").value("INC-3001"))
            .andExpect(jsonPath("$.serviceId").value("payments-api"))
            .andExpect(jsonPath("$.summary").value("queue delay"));
    }

    @Test
    void getById_returns200ForExistingIncident() throws Exception {
        when(incidentService.getById(eq("INC-4001"))).thenReturn(
            new IncidentEntity("INC-4001", "identity-api", 3, "token issue", "OPEN")
        );

        mvc.perform(get("/api/v2/incidents/INC-4001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.incidentId").value("INC-4001"))
            .andExpect(jsonPath("$.serviceId").value("identity-api"));
    }

    @Test
    void validationErrors_return400WithDetails() throws Exception {
        mvc.perform(post("/api/v2/incidents")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "serviceId": " ",
                      "severity": 9,
                      "summary": " "
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.details.serviceId").exists())
            .andExpect(jsonPath("$.details.summary").exists())
            .andExpect(jsonPath("$.details.severity").exists());
    }

    @Test
    void pathVariableConstraintViolation_returns400() throws Exception {
        GlobalApiExceptionHandler handler = new GlobalApiExceptionHandler();
        ResponseEntity<?> response = handler.handleConstraintViolation(
            new ConstraintViolationException("incidentId must match INC-<digits>", java.util.Set.of())
        );
        assertEquals(400, response.getStatusCode().value());
        assertEquals("CONSTRAINT_VIOLATION", ((com.showoff.incidentops.springboot.rest.exception.ApiErrorResponse) response.getBody()).code());
    }

    @Test
    void domainAndGenericExceptions_areHandledCentrally() throws Exception {
        when(incidentService.getById(eq("INC-5001"))).thenThrow(new IncidentNotFoundException("incident not found: INC-5001"));
        when(incidentService.getById(eq("INC-5002"))).thenThrow(new IllegalArgumentException("incidentId must not be blank"));
        when(incidentService.getById(eq("INC-5003"))).thenThrow(new RuntimeException("unexpected"));

        mvc.perform(get("/api/v2/incidents/INC-5001"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("INCIDENT_NOT_FOUND"));

        mvc.perform(get("/api/v2/incidents/INC-5002"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("BAD_REQUEST"));

        mvc.perform(get("/api/v2/incidents/INC-5003"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));
    }
}
