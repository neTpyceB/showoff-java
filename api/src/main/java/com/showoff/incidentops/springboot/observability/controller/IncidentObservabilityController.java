package com.showoff.incidentops.springboot.observability.controller;

import com.showoff.incidentops.springboot.observability.dto.IncidentProcessingResponse;
import com.showoff.incidentops.springboot.observability.service.IncidentObservabilityService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v7/observability/incidents")
public class IncidentObservabilityController {
    private final IncidentObservabilityService observabilityService;

    public IncidentObservabilityController(IncidentObservabilityService observabilityService) {
        this.observabilityService = observabilityService;
    }

    @PostMapping("/{incidentId}/process")
    public ResponseEntity<IncidentProcessingResponse> process(
        @PathVariable("incidentId")
        @Pattern(regexp = "INC-\\d+", message = "incidentId must match INC-<digits>")
        String incidentId,
        @RequestParam("severity")
        @Min(value = 1, message = "severity must be between 1 and 5")
        @Max(value = 5, message = "severity must be between 1 and 5")
        int severity,
        @RequestParam(name = "simulateFailure", defaultValue = "false")
        boolean simulateFailure
    ) {
        IncidentProcessingResponse response = observabilityService.processIncident(incidentId, severity, simulateFailure);
        return ResponseEntity.accepted().body(response);
    }
}
