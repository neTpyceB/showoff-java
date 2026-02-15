package com.showoff.incidentops.streams;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class IncidentOpsStreamAnalytics {
    private IncidentOpsStreamAnalytics() {}

    public record IncidentEvent(
        String incidentId,
        String serviceId,
        int severity,
        String ownerTeam,
        int latencyMs
    ) {}

    public static List<String> sortedCriticalIncidentIds(List<IncidentEvent> incidents) {
        validateIncidents(incidents);
        return incidents.stream()
            .filter(event -> event.severity() >= 4)
            .map(IncidentEvent::incidentId)
            .sorted()
            .toList();
    }

    public static List<String> highLatencyServiceAlerts(
        Map<String, Integer> serviceLatencyMs,
        int thresholdMs,
        Consumer<String> notifier
    ) {
        validateLatencyByService(serviceLatencyMs);
        if (thresholdMs < 0) {
            throw new IllegalArgumentException("thresholdMs must be >= 0");
        }
        if (notifier == null) {
            throw new IllegalArgumentException("notifier must not be null");
        }

        List<String> alerts = serviceLatencyMs.entrySet().stream()
            .filter(entry -> entry.getValue() > thresholdMs)
            .sorted(latencyComparator(true))
            .map(entry -> "ALERT service=" + entry.getKey() + " latencyMs=" + entry.getValue())
            .toList();

        alerts.forEach(notifier);
        return alerts;
    }

    public static Map<String, List<String>> incidentIdsByService(List<IncidentEvent> incidents) {
        validateIncidents(incidents);
        return incidents.stream()
            .collect(Collectors.groupingBy(
                IncidentEvent::serviceId,
                LinkedHashMap::new,
                Collectors.mapping(IncidentEvent::incidentId, Collectors.toList())
            ));
    }

    public static Map<Integer, Long> incidentCountBySeverity(List<IncidentEvent> incidents) {
        validateIncidents(incidents);
        return incidents.stream()
            .collect(Collectors.groupingBy(
                IncidentEvent::severity,
                LinkedHashMap::new,
                Collectors.counting()
            ));
    }

    public static List<String> ownerTeamsAlphabetical(List<IncidentEvent> incidents) {
        validateIncidents(incidents);
        return incidents.stream()
            .map(IncidentEvent::ownerTeam)
            .map(String::trim)
            .map(String::toLowerCase)
            .distinct()
            .sorted()
            .collect(Collectors.toCollection(ArrayList::new));
    }

    public static int totalLatencyMs(List<IncidentEvent> incidents) {
        validateIncidents(incidents);
        return incidents.stream()
            .map(IncidentEvent::latencyMs)
            .reduce(0, Integer::sum);
    }

    public static Optional<String> slowestServiceId(Map<String, Integer> serviceLatencyMs) {
        validateLatencyByService(serviceLatencyMs);
        return serviceLatencyMs.entrySet().stream()
            .max(latencyComparator(false))
            .map(Map.Entry::getKey);
    }

    public static List<String> sortServicesByLatency(Map<String, Integer> serviceLatencyMs, boolean descending) {
        validateLatencyByService(serviceLatencyMs);
        return serviceLatencyMs.entrySet().stream()
            .sorted(latencyComparator(descending))
            .map(Map.Entry::getKey)
            .toList();
    }

    private static Comparator<Map.Entry<String, Integer>> latencyComparator(boolean descending) {
        Comparator<Map.Entry<String, Integer>> comparator = Comparator
            .comparingInt(Map.Entry<String, Integer>::getValue)
            .thenComparing(Map.Entry<String, Integer>::getKey);
        return descending ? comparator.reversed() : comparator;
    }

    private static void validateIncidents(List<IncidentEvent> incidents) {
        if (incidents == null) {
            throw new IllegalArgumentException("incidents must not be null");
        }
        for (IncidentEvent event : incidents) {
            if (event == null) {
                throw new IllegalArgumentException("incidents must not contain null");
            }
            validateNonBlank(event.incidentId(), "incidentId");
            validateNonBlank(event.serviceId(), "serviceId");
            validateNonBlank(event.ownerTeam(), "ownerTeam");
            if (event.severity() < 1 || event.severity() > 5) {
                throw new IllegalArgumentException("severity must be 1..5");
            }
            if (event.latencyMs() < 0) {
                throw new IllegalArgumentException("latencyMs must be >= 0");
            }
        }
    }

    private static void validateLatencyByService(Map<String, Integer> serviceLatencyMs) {
        if (serviceLatencyMs == null) {
            throw new IllegalArgumentException("serviceLatencyMs must not be null");
        }
        for (Map.Entry<String, Integer> entry : serviceLatencyMs.entrySet()) {
            validateNonBlank(entry.getKey(), "serviceId");
            Integer latency = entry.getValue();
            if (latency == null || latency < 0) {
                throw new IllegalArgumentException("latencyMs must be >= 0");
            }
        }
    }

    private static void validateNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must be non-blank");
        }
    }
}
