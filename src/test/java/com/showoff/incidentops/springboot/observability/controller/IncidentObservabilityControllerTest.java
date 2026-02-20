package com.showoff.incidentops.springboot.observability.controller;

import com.showoff.incidentops.springboot.observability.dto.IncidentProcessingResponse;
import com.showoff.incidentops.springboot.observability.service.IncidentObservabilityService;
import com.showoff.incidentops.springboot.rest.exception.GlobalApiExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class IncidentObservabilityControllerTest {
    private IncidentObservabilityService observabilityService;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        observabilityService = mock(IncidentObservabilityService.class);
        IncidentObservabilityController controller = new IncidentObservabilityController(observabilityService);
        mvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalApiExceptionHandler())
            .build();
    }

    @Test
    void process_returnsAcceptedResponse() throws Exception {
        when(observabilityService.processIncident(eq("INC-5401"), eq(4), eq(false))).thenReturn(
            new IncidentProcessingResponse("INC-5401", 4, "SUCCESS", 12)
        );

        mvc.perform(post("/api/v7/observability/incidents/INC-5401/process")
                .param("severity", "4"))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.incidentId").value("INC-5401"))
            .andExpect(jsonPath("$.severity").value(4))
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.durationMs").value(12));
    }

    @Test
    void process_mapsFailuresToErrorResponse() throws Exception {
        doThrow(new IllegalStateException("downstream timeout")).when(observabilityService)
            .processIncident(eq("INC-5402"), eq(5), eq(true));

        mvc.perform(post("/api/v7/observability/incidents/INC-5402/process")
                .param("severity", "5")
                .param("simulateFailure", "true"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));
    }

    @Test
    void process_illegalArgument_mapsToBadRequest() throws Exception {
        doThrow(new IllegalArgumentException("severity must be between 1 and 5")).when(observabilityService)
            .processIncident(eq("INC-5403"), eq(4), eq(false));

        mvc.perform(post("/api/v7/observability/incidents/INC-5403/process")
                .param("severity", "4"))
            .andExpect(status().isBadRequest());
    }
}
