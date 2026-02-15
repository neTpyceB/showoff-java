package com.showoff.incidentops.reliability;

public class IncidentValidationException extends RuntimeException {
    public IncidentValidationException(String message) {
        super(message);
    }

    public IncidentValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
