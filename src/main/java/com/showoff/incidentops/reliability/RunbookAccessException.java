package com.showoff.incidentops.reliability;

public class RunbookAccessException extends Exception {
    public RunbookAccessException(String message) {
        super(message);
    }

    public RunbookAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
