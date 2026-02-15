package com.showoff.incidentops.springboot;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class IncidentControllerTest {
    private IncidentService incidentService;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        incidentService = mock(IncidentService.class);
        IncidentController controller = new IncidentController(incidentService);
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void health_returnsJsonResponse() throws Exception {
        mvc.perform(get("/api/incidents/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.component").value("incident-api"));
    }

    @Test
    void getIncident_usesPathVariable() throws Exception {
        when(incidentService.getIncident(eq("INC-7777"))).thenReturn(
            new IncidentResponse("INC-7777", "payments-api", 4, "queue delay", "OPEN")
        );

        mvc.perform(get("/api/incidents/INC-7777"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.incidentId").value("INC-7777"))
            .andExpect(jsonPath("$.serviceId").value("payments-api"))
            .andExpect(jsonPath("$.severity").value(4))
            .andExpect(jsonPath("$.summary").value("queue delay"))
            .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void createIncident_usesPostAndRequestBody() throws Exception {
        when(incidentService.createIncident(any(IncidentRequest.class))).thenReturn(
            new IncidentResponse("INC-9001", "identity-api", 3, "token issue", "OPEN")
        );

        mvc.perform(post("/api/incidents")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "serviceId": "identity-api",
                      "severity": 3,
                      "summary": "token issue"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.incidentId").value("INC-9001"))
            .andExpect(jsonPath("$.serviceId").value("identity-api"))
            .andExpect(jsonPath("$.summary").value("token issue"));
    }
}
