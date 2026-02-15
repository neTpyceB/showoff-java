package com.showoff.incidentops.springboot.persistence.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateIncidentTicketRequest(
    @NotBlank(message = "serviceId must not be blank")
    @Size(max = 64, message = "serviceId must be <= 64 chars")
    String serviceId,
    @Min(value = 1, message = "severity must be between 1 and 5")
    @Max(value = 5, message = "severity must be between 1 and 5")
    int severity,
    @NotBlank(message = "summary must not be blank")
    @Size(max = 255, message = "summary must be <= 255 chars")
    String summary
) {}
