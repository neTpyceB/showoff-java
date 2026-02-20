package com.showoff.incidentops.springboot.rest.controller;

import com.showoff.incidentops.springboot.rest.dto.CreateIncidentRequest;
import com.showoff.incidentops.springboot.rest.dto.IncidentResponseDto;
import com.showoff.incidentops.springboot.rest.model.IncidentEntity;
import com.showoff.incidentops.springboot.rest.service.IncidentCommandService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v2/incidents")
public class IncidentRestController {
    private final IncidentCommandService incidentService;

    public IncidentRestController(IncidentCommandService incidentService) {
        this.incidentService = incidentService;
    }

    @PostMapping
    public ResponseEntity<IncidentResponseDto> create(@Valid @RequestBody CreateIncidentRequest request) {
        IncidentEntity entity = incidentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(entity));
    }

    @GetMapping("/{incidentId}")
    public IncidentResponseDto getById(
        @PathVariable("incidentId")
        @Pattern(regexp = "INC-\\d+", message = "incidentId must match INC-<digits>")
        String incidentId
    ) {
        return toDto(incidentService.getById(incidentId));
    }

    private static IncidentResponseDto toDto(IncidentEntity entity) {
        return new IncidentResponseDto(
            entity.incidentId(),
            entity.serviceId(),
            entity.severity(),
            entity.summary(),
            entity.status()
        );
    }
}
