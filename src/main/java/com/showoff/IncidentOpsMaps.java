package com.showoff;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class IncidentOpsMaps {
    private IncidentOpsMaps() {}

    public static Map<String, Integer> defaultSloTargets() {
        Map<String, Integer> targets = new LinkedHashMap<>();
        targets.put("payments-api", 99);
        targets.put("identity-api", 99);
        targets.put("checkout-api", 98);
        return targets;
    }

    public static Integer upsertSloTarget(Map<String, Integer> targets, String serviceId, int targetPercent) {
        if (targets == null) {
            throw new IllegalArgumentException("targets must not be null");
        }
        if (targetPercent < 1 || targetPercent > 100) {
            throw new IllegalArgumentException("targetPercent must be 1..100");
        }
        validateNonBlank(serviceId, "serviceId");
        return targets.put(serviceId, targetPercent);
    }

    public static String findPagerChannel(Map<String, String> serviceToChannel, String serviceId) {
        if (serviceToChannel == null) {
            throw new IllegalArgumentException("serviceToChannel must not be null");
        }
        validateNonBlank(serviceId, "serviceId");
        return serviceToChannel.getOrDefault(serviceId, "#incident-command");
    }

    public static boolean addSuppressionTagIfMissing(Map<String, String> incidentToTag, String incidentId, String tag) {
        if (incidentToTag == null) {
            throw new IllegalArgumentException("incidentToTag must not be null");
        }
        validateNonBlank(incidentId, "incidentId");
        validateNonBlank(tag, "tag");
        return incidentToTag.putIfAbsent(incidentId, tag) == null;
    }

    public static int incrementErrorBudgetBurn(Map<String, Integer> serviceToBurn, String serviceId) {
        if (serviceToBurn == null) {
            throw new IllegalArgumentException("serviceToBurn must not be null");
        }
        validateNonBlank(serviceId, "serviceId");
        return serviceToBurn.merge(serviceId, 1, Integer::sum);
    }

    public static List<String> timelineForIncident(
        Map<String, List<String>> incidentToTimeline,
        String incidentId,
        String event
    ) {
        if (incidentToTimeline == null) {
            throw new IllegalArgumentException("incidentToTimeline must not be null");
        }
        validateNonBlank(incidentId, "incidentId");
        validateNonBlank(event, "event");

        List<String> timeline = incidentToTimeline.computeIfAbsent(incidentId, key -> new ArrayList<>());
        timeline.add(event);
        return timeline;
    }

    public static void normalizeStatuses(Map<String, String> incidentToStatus) {
        if (incidentToStatus == null) {
            throw new IllegalArgumentException("incidentToStatus must not be null");
        }
        incidentToStatus.replaceAll((incidentId, status) -> {
            validateNonBlank(incidentId, "incidentId");
            validateNonBlank(status, "status");
            return status.trim().toUpperCase();
        });
    }

    public static int removeClosedIncidents(Map<String, String> incidentToStatus) {
        if (incidentToStatus == null) {
            throw new IllegalArgumentException("incidentToStatus must not be null");
        }
        int removed = 0;
        Iterator<Map.Entry<String, String>> iterator = incidentToStatus.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            if ("CLOSED".equals(entry.getValue())) {
                iterator.remove();
                removed++;
            }
        }
        return removed;
    }

    public static String summarizeOwnership(Map<String, String> serviceToOwner) {
        if (serviceToOwner == null) {
            throw new IllegalArgumentException("serviceToOwner must not be null");
        }
        StringBuilder summary = new StringBuilder();
        for (Map.Entry<String, String> entry : serviceToOwner.entrySet()) {
            validateNonBlank(entry.getKey(), "serviceId");
            validateNonBlank(entry.getValue(), "owner");
            if (!summary.isEmpty()) {
                summary.append(" | ");
            }
            summary.append(entry.getKey()).append("->").append(entry.getValue());
        }
        return summary.toString();
    }

    public static Map<String, String> immutableRoutingSnapshot(Map<String, String> serviceToChannel) {
        if (serviceToChannel == null) {
            throw new IllegalArgumentException("serviceToChannel must not be null");
        }
        return Map.copyOf(serviceToChannel);
    }

    private static void validateNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must be non-blank");
        }
    }
}
