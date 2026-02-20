package com.showoff.incidentops.springboot.observability.integration;

import com.showoff.incidentops.springboot.IncidentApiApplication;
import com.showoff.incidentops.springboot.observability.service.IncidentObservabilityService;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
    classes = IncidentApiApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.main.banner-mode=off",
        "management.health.redis.enabled=false"
    }
)
class ObservabilityActuatorIntegrationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private IncidentObservabilityService observabilityService;

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    void healthEndpoint_isAvailable() throws IOException, InterruptedException {
        HttpResponse<String> response = httpGet("/actuator/health");

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"status\":\"UP\""));
    }

    @Test
    void customMetrics_areVisibleOnActuator() {
        observabilityService.processIncident("INC-9901", 3, false);

        assertTrue(meterRegistry.find("incidentops.incident.processed").counter().count() >= 1.0d);
        assertTrue(meterRegistry.find("incidentops.incident.processing").timer().count() >= 1L);
    }

    private HttpResponse<String> httpGet(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + path))
            .GET()
            .build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }
}
