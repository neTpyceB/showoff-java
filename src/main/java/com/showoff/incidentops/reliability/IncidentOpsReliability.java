package com.showoff.incidentops.reliability;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class IncidentOpsReliability {
    private IncidentOpsReliability() {}

    public interface RunbookSource extends AutoCloseable {
        String readLine() throws IOException;

        @Override
        void close() throws IOException;
    }

    public static int parseRetryBudgetOrDefault(String rawBudget, int fallback) {
        if (rawBudget == null || rawBudget.isBlank()) {
            return fallback;
        }
        try {
            int parsed = Integer.parseInt(rawBudget.trim());
            return parsed < 0 ? fallback : parsed;
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    public static int parseTimeoutSeconds(String timeoutMillis) {
        validateNonBlank(timeoutMillis, "timeoutMillis");
        try {
            long millis = Long.parseLong(timeoutMillis.trim());
            if (millis < 0) {
                throw new IncidentValidationException("timeoutMillis must be >= 0");
            }
            return Math.toIntExact(millis / 1000);
        } catch (NumberFormatException | ArithmeticException ex) {
            throw new IncidentValidationException("timeoutMillis is invalid", ex);
        }
    }

    public static String normalizeIncidentId(String incidentId) {
        try {
            validateNonBlank(incidentId, "incidentId");
            return incidentId.trim().toUpperCase();
        } catch (IllegalArgumentException ex) {
            throw new IncidentValidationException("incidentId is invalid", ex);
        }
    }

    public static String loadRunbookStep(Map<String, String> runbookByService, String serviceId) throws RunbookAccessException {
        if (runbookByService == null) {
            throw new IllegalArgumentException("runbookByService must not be null");
        }
        validateNonBlank(serviceId, "serviceId");
        String step = runbookByService.get(serviceId);
        if (step == null || step.isBlank()) {
            throw new RunbookAccessException("no runbook step for " + serviceId);
        }
        return step.trim();
    }

    public static List<String> parseRunbookActions(RunbookSource source) throws RunbookAccessException {
        if (source == null) {
            throw new IllegalArgumentException("source must not be null");
        }
        List<String> actions = new ArrayList<>();
        try (source) {
            String line;
            while ((line = source.readLine()) != null) {
                String normalized = line.trim();
                if (normalized.isEmpty() || normalized.startsWith("#")) {
                    continue;
                }
                actions.add(normalized);
            }
            return actions;
        } catch (IOException ex) {
            throw new RunbookAccessException("failed to read runbook actions", ex);
        }
    }

    public static Optional<String> firstReachableChannel(List<Optional<String>> channels) {
        if (channels == null) {
            throw new IllegalArgumentException("channels must not be null");
        }
        if (channels.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("channels must not contain null optionals");
        }
        return channels.stream()
            .flatMap(Optional::stream)
            .map(String::trim)
            .filter(value -> !value.isBlank())
            .map(String::toLowerCase)
            .findFirst();
    }

    private static void validateNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must be non-blank");
        }
    }
}
