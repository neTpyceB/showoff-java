package com.showoff.incidentops.springboot.messaging.controller;

import com.showoff.incidentops.springboot.messaging.dto.IncidentCreatedEvent;
import com.showoff.incidentops.springboot.messaging.dto.PublishIncidentEventRequest;
import com.showoff.incidentops.springboot.messaging.service.IncidentEventPublisher;
import com.showoff.incidentops.springboot.rest.exception.GlobalApiExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class IncidentEventControllerTest {
    private IncidentEventPublisher publisher;
    private MockMvc mvc;
    private LocalValidatorFactoryBean validator;

    @BeforeEach
    void setUp() {
        publisher = mock(IncidentEventPublisher.class);
        IncidentEventController controller = new IncidentEventController(publisher);
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
    void publishIncidentCreated_returnsAcceptedResponse() throws Exception {
        mvc.perform(post("/api/v5/events/incidents/created")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "incidentId": "inc-7001",
                      "serviceId": "payments-api",
                      "severity": 4
                    }
                    """))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.incidentId").value("INC-7001"))
            .andExpect(jsonPath("$.serviceId").value("payments-api"))
            .andExpect(jsonPath("$.severity").value(4));
    }

    @Test
    void publishIncidentCreated_returnsValidationAndErrorResponses() throws Exception {
        mvc.perform(post("/api/v5/events/incidents/created")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "incidentId": " ",
                      "serviceId": " ",
                      "severity": 9
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        doThrow(new IllegalArgumentException("publisher unavailable")).when(publisher).publishIncidentCreated(any());
        mvc.perform(post("/api/v5/events/incidents/created")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "incidentId": "inc-7002",
                      "serviceId": "payments-api",
                      "severity": 3
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }
}
