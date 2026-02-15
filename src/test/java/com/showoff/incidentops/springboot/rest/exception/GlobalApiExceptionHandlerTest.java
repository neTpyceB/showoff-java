package com.showoff.incidentops.springboot.rest.exception;

import com.showoff.incidentops.springboot.persistence.exception.IncidentTicketNotFoundException;
import com.showoff.incidentops.springboot.rest.pipeline.ApiCorrelationFilter;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalApiExceptionHandlerTest {
    @Test
    void handler_usesCorrelationIdFromRequestAttribute() {
        GlobalApiExceptionHandler handler = new GlobalApiExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v2/incidents/INC-9");
        request.setAttribute(ApiCorrelationFilter.CORRELATION_ID_ATTRIBUTE, "REQ-123");

        ResponseEntity<ApiErrorResponse> response = handler.handleBadRequest(
            new IllegalArgumentException("bad input"),
            request
        );

        assertEquals(400, response.getStatusCode().value());
        assertEquals("BAD_REQUEST", response.getBody().code());
        assertEquals("/api/v2/incidents/INC-9", response.getBody().path());
        assertEquals("req-123", response.getBody().correlationId());
        assertTrue(response.getBody().timestamp().contains("T"));
    }

    @Test
    void handler_usesHeaderCorrelationWhenAttributeMissing() {
        GlobalApiExceptionHandler handler = new GlobalApiExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v4/tickets/TKT-1");
        request.addHeader(ApiCorrelationFilter.CORRELATION_ID_HEADER, "CID-001");

        ResponseEntity<ApiErrorResponse> response = handler.handleTicketNotFound(
            new IncidentTicketNotFoundException("missing ticket"),
            request
        );

        assertEquals(404, response.getStatusCode().value());
        assertEquals("TICKET_NOT_FOUND", response.getBody().code());
        assertEquals("cid-001", response.getBody().correlationId());
    }

    @Test
    void handler_fallsBackToNaCorrelationAndPathWhenMissing() {
        GlobalApiExceptionHandler handler = new GlobalApiExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "");
        request.setAttribute(ApiCorrelationFilter.CORRELATION_ID_ATTRIBUTE, " ");
        request.addHeader(ApiCorrelationFilter.CORRELATION_ID_HEADER, " ");

        ResponseEntity<ApiErrorResponse> response = handler.handleConstraintViolation(
            new ConstraintViolationException("constraint failed", java.util.Set.of()),
            request
        );

        assertEquals(400, response.getStatusCode().value());
        assertEquals("CONSTRAINT_VIOLATION", response.getBody().code());
        assertEquals("n/a", response.getBody().path());
        assertEquals("n/a", response.getBody().correlationId());
    }
}
