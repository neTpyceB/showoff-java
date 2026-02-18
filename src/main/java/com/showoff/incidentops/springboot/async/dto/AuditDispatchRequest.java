package com.showoff.incidentops.springboot.async.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuditDispatchRequest(
    @NotBlank(message = "requestedBy must not be blank")
    @Size(max = 120, message = "requestedBy must be at most 120 characters")
    String requestedBy,

    @NotBlank(message = "reason must not be blank")
    @Size(max = 500, message = "reason must be at most 500 characters")
    String reason
) {}
