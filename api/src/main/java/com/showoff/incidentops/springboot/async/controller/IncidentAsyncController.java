package com.showoff.incidentops.springboot.async.controller;

import com.showoff.incidentops.springboot.async.dto.AuditDispatchRequest;
import com.showoff.incidentops.springboot.async.dto.AuditDispatchResponse;
import com.showoff.incidentops.springboot.async.dto.ImpactScoreResponse;
import com.showoff.incidentops.springboot.async.service.IncidentAsyncProcessingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Validated
@RestController
@RequestMapping("/api/v6/async/incidents")
public class IncidentAsyncController {
    private final IncidentAsyncProcessingService asyncService;

    public IncidentAsyncController(IncidentAsyncProcessingService asyncService) {
        this.asyncService = asyncService;
    }

    @PostMapping("/{incidentId}/audits")
    public ResponseEntity<AuditDispatchResponse> queueAudit(
        @PathVariable("incidentId")
        @Pattern(regexp = "INC-\\d+", message = "incidentId must match INC-<digits>")
        String incidentId,
        @Valid @RequestBody AuditDispatchRequest request
    ) {
        String normalizedIncidentId = incidentId.trim().toUpperCase();
        asyncService.dispatchAuditAsync(normalizedIncidentId, request.requestedBy(), request.reason());

        AuditDispatchResponse response = new AuditDispatchResponse(
            UUID.randomUUID().toString(),
            normalizedIncidentId,
            "QUEUED"
        );
        return ResponseEntity.accepted().body(response);
    }

    @GetMapping("/{incidentId}/impact-score")
    public CompletableFuture<ResponseEntity<ImpactScoreResponse>> calculateImpactScore(
        @PathVariable("incidentId")
        @Pattern(regexp = "INC-\\d+", message = "incidentId must match INC-<digits>")
        String incidentId,
        @RequestParam("severity")
        @Min(value = 1, message = "severity must be between 1 and 5")
        @Max(value = 5, message = "severity must be between 1 and 5")
        int severity
    ) {
        String normalizedIncidentId = incidentId.trim().toUpperCase();
        return asyncService.calculateImpactScoreAsync(normalizedIncidentId, severity)
            .thenApply(ResponseEntity::ok);
    }
}
