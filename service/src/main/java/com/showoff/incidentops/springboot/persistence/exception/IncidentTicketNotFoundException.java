package com.showoff.incidentops.springboot.persistence.exception;

public class IncidentTicketNotFoundException extends RuntimeException {
    public IncidentTicketNotFoundException(String message) {
        super(message);
    }
}
