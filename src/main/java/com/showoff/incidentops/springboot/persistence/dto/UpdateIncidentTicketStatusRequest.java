package com.showoff.incidentops.springboot.persistence.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateIncidentTicketStatusRequest(
    @NotBlank(message = "status must not be blank")
    @Size(max = 32, message = "status must be <= 32 chars")
    String status
) {}
