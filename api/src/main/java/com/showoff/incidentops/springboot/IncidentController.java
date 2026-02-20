package com.showoff.incidentops.springboot;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {
    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "component", "incident-api");
    }

    @GetMapping("/{incidentId}")
    public IncidentResponse getIncident(@PathVariable("incidentId") String incidentId) {
        return incidentService.getIncident(incidentId);
    }

    @PostMapping
    public IncidentResponse createIncident(@RequestBody IncidentRequest request) {
        return incidentService.createIncident(request);
    }
}
