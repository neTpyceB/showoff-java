package com.showoff.incidentops.springboot.async.controller;

import com.showoff.incidentops.springboot.async.dto.ImpactScoreResponse;
import com.showoff.incidentops.springboot.async.service.IncidentAsyncProcessingService;
import com.showoff.incidentops.springboot.rest.exception.GlobalApiExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class IncidentAsyncControllerTest {
    private IncidentAsyncProcessingService asyncService;
    private MockMvc mvc;
    private LocalValidatorFactoryBean validator;

    @BeforeEach
    void setUp() {
        asyncService = mock(IncidentAsyncProcessingService.class);
        IncidentAsyncController controller = new IncidentAsyncController(asyncService);

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
    void queueAudit_returnsAcceptedAndDelegatesToAsyncService() throws Exception {
        mvc.perform(post("/api/v6/async/incidents/INC-8801/audits")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "requestedBy": "sre.oncall",
                      "reason": "trigger post-incident audit"
                    }
                    """))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.incidentId").value("INC-8801"))
            .andExpect(jsonPath("$.status").value("QUEUED"))
            .andExpect(jsonPath("$.dispatchId").isString());

        verify(asyncService).dispatchAuditAsync(eq("INC-8801"), eq("sre.oncall"), eq("trigger post-incident audit"));
    }

    @Test
    void calculateImpactScore_returnsAsyncComputationResult() throws Exception {
        when(asyncService.calculateImpactScoreAsync(eq("INC-8802"), eq(5))).thenReturn(CompletableFuture.completedFuture(
            new ImpactScoreResponse("INC-8802", 5, 96, "2026-02-18T15:00:00Z", "incidentops-async-1")
        ));

        var mvcResult = mvc.perform(get("/api/v6/async/incidents/INC-8802/impact-score")
                .param("severity", "5"))
            .andExpect(request().asyncStarted())
            .andReturn();

        mvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.incidentId").value("INC-8802"))
            .andExpect(jsonPath("$.severity").value(5))
            .andExpect(jsonPath("$.impactScore").value(96));
    }

    @Test
    void controllerValidation_returnsBadRequestForInvalidInput() throws Exception {
        mvc.perform(post("/api/v6/async/incidents/INVALID-ID/audits")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "requestedBy": " ",
                      "reason": " "
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
