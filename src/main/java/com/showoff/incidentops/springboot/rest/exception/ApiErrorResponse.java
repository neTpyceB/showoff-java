package com.showoff.incidentops.springboot.rest.exception;

import java.util.Map;

public record ApiErrorResponse(String code, String message, Map<String, String> details) {}
