package com.showoff.incidentops.springboot.persistence.controller;

import com.showoff.incidentops.springboot.persistence.dto.CreateIncidentTicketRequest;
import com.showoff.incidentops.springboot.persistence.dto.IncidentTicketResponse;
import com.showoff.incidentops.springboot.persistence.service.IncidentTicketService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v4/tickets")
public class IncidentTicketController {
    private final IncidentTicketService ticketService;

    public IncidentTicketController(IncidentTicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<IncidentTicketResponse> create(@Valid @RequestBody CreateIncidentTicketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.create(request));
    }

    @GetMapping("/{ticketId}")
    public IncidentTicketResponse getById(
        @PathVariable("ticketId")
        @Pattern(regexp = "TKT-\\d+", message = "ticketId must match TKT-<digits>")
        String ticketId
    ) {
        return ticketService.getByTicketId(ticketId);
    }

    @GetMapping
    public Page<IncidentTicketResponse> listByStatus(
        @RequestParam(name = "status", defaultValue = "OPEN")
        @NotBlank(message = "status must not be blank")
        String status,
        @RequestParam(name = "page", defaultValue = "0")
        @Min(value = 0, message = "page must be >= 0")
        int page,
        @RequestParam(name = "size", defaultValue = "20")
        @Min(value = 1, message = "size must be >= 1")
        @Max(value = 100, message = "size must be <= 100")
        int size
    ) {
        return ticketService.listByStatus(status, page, size);
    }

    @GetMapping("/search")
    public Page<IncidentTicketResponse> searchByServiceAndSeverity(
        @RequestParam("serviceId")
        @NotBlank(message = "serviceId must not be blank")
        String serviceId,
        @RequestParam(name = "minSeverity", defaultValue = "1")
        @Min(value = 1, message = "minSeverity must be between 1 and 5")
        @Max(value = 5, message = "minSeverity must be between 1 and 5")
        int minSeverity,
        @RequestParam(name = "page", defaultValue = "0")
        @Min(value = 0, message = "page must be >= 0")
        int page,
        @RequestParam(name = "size", defaultValue = "20")
        @Min(value = 1, message = "size must be >= 1")
        @Max(value = 100, message = "size must be <= 100")
        int size
    ) {
        return ticketService.searchByServiceAndMinSeverity(serviceId, minSeverity, page, size);
    }
}
