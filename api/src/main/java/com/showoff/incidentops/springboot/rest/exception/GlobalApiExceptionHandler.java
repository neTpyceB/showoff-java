package com.showoff.incidentops.springboot.rest.exception;

import com.showoff.incidentops.springboot.persistence.exception.IncidentTicketNotFoundException;
import com.showoff.incidentops.springboot.rest.pipeline.ApiCorrelationFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.time.Instant;

@RestControllerAdvice
public class GlobalApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
        MethodArgumentNotValidException ex,
        HttpServletRequest request
    ) {
        Map<String, String> details = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            details.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return buildError(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "request validation failed", details, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
        ConstraintViolationException ex,
        HttpServletRequest request
    ) {
        return buildError(
            HttpStatus.BAD_REQUEST,
            "CONSTRAINT_VIOLATION",
            "constraint validation failed",
            Map.of("violation", ex.getMessage()),
            request
        );
    }

    @ExceptionHandler(IncidentNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(IncidentNotFoundException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "INCIDENT_NOT_FOUND", ex.getMessage(), Map.of(), request);
    }

    @ExceptionHandler(IncidentTicketNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleTicketNotFound(
        IncidentTicketNotFoundException ex,
        HttpServletRequest request
    ) {
        return buildError(HttpStatus.NOT_FOUND, "TICKET_NOT_FOUND", ex.getMessage(), Map.of(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(IllegalArgumentException ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), Map.of(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        return buildError(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_ERROR",
            "unexpected server error",
            Map.of(),
            request
        );
    }

    private static ResponseEntity<ApiErrorResponse> buildError(
        HttpStatus status,
        String code,
        String message,
        Map<String, String> details,
        HttpServletRequest request
    ) {
        String path = request.getRequestURI();
        String correlationId = "n/a";
        Object correlationAttribute = request.getAttribute(ApiCorrelationFilter.CORRELATION_ID_ATTRIBUTE);
        if (correlationAttribute instanceof String value && !value.isBlank()) {
            correlationId = value;
        } else {
            String header = request.getHeader(ApiCorrelationFilter.CORRELATION_ID_HEADER);
            if (header != null && !header.isBlank()) {
                correlationId = header.trim();
            }
        }
        return ResponseEntity.status(status).body(
            new ApiErrorResponse(
                code,
                message,
                details,
                path.isBlank() ? "n/a" : path,
                correlationId.toLowerCase(Locale.ROOT),
                Instant.now().toString()
            )
        );
    }
}
