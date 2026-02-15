package com.showoff.incidentops.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IncidentOpsLogger {
    private static final Logger LOG = LoggerFactory.getLogger(IncidentOpsLogger.class);

    private IncidentOpsLogger() {}

    public static void incidentOpened(String incidentId, String serviceId) {
        validateNonBlank(incidentId, "incidentId");
        validateNonBlank(serviceId, "serviceId");
        LOG.info("incident opened: id={}, service={}", incidentId.trim().toUpperCase(), serviceId.trim().toLowerCase());
    }

    public static void incidentResolved(String incidentId, long durationSeconds) {
        validateNonBlank(incidentId, "incidentId");
        if (durationSeconds < 0) {
            throw new IllegalArgumentException("durationSeconds must be >= 0");
        }
        LOG.info("incident resolved: id={}, durationSeconds={}", incidentId.trim().toUpperCase(), durationSeconds);
    }

    public static void notificationAttempt(String incidentId, int attempt, boolean delivered) {
        validateNonBlank(incidentId, "incidentId");
        if (attempt <= 0) {
            throw new IllegalArgumentException("attempt must be > 0");
        }
        if (delivered) {
            LOG.info("notification delivered: id={}, attempt={}", incidentId.trim().toUpperCase(), attempt);
        } else {
            LOG.warn("notification failed: id={}, attempt={}", incidentId.trim().toUpperCase(), attempt);
        }
    }

    public static int samplingRateOrDefault(String rawPercent, int fallbackPercent) {
        if (fallbackPercent < 0 || fallbackPercent > 100) {
            throw new IllegalArgumentException("fallbackPercent must be 0..100");
        }
        if (rawPercent == null || rawPercent.isBlank()) {
            LOG.debug("sampling rate missing; using fallback={}", fallbackPercent);
            return fallbackPercent;
        }
        try {
            int parsed = Integer.parseInt(rawPercent.trim());
            if (parsed < 0 || parsed > 100) {
                LOG.warn("sampling rate out of range; value={}, fallback={}", parsed, fallbackPercent);
                return fallbackPercent;
            }
            return parsed;
        } catch (NumberFormatException ex) {
            LOG.warn("sampling rate invalid; value={}, fallback={}", rawPercent, fallbackPercent);
            return fallbackPercent;
        }
    }

    private static void validateNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must be non-blank");
        }
    }
}
