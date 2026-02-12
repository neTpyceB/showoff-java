package com.showoff;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public static List<String> servicesByDescendingBurn(Map<String, Integer> serviceToBurn) {
        if (serviceToBurn == null) {
            throw new IllegalArgumentException("serviceToBurn must not be null");
        }
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(serviceToBurn.size());
        for (Map.Entry<String, Integer> entry : serviceToBurn.entrySet()) {
            validateNonBlank(entry.getKey(), "serviceId");
            if (entry.getValue() == null || entry.getValue() < 0) {
                throw new IllegalArgumentException("burn value must be >= 0");
            }
            entries.add(entry);
        }
        entries.sort((a, b) -> {
            int byBurn = Integer.compare(b.getValue(), a.getValue());
            if (byBurn != 0) {
                return byBurn;
            }
            return a.getKey().compareTo(b.getKey());
        });
        List<String> services = new ArrayList<>(entries.size());
        for (Map.Entry<String, Integer> entry : entries) {
            services.add(entry.getKey());
        }
        return services;
    }

    public static Map<String, String> mergeRoutingOverrides(
        Map<String, String> baseRouting,
        Map<String, String> overrideRouting
    ) {
        if (baseRouting == null) {
            throw new IllegalArgumentException("baseRouting must not be null");
        }
        if (overrideRouting == null) {
            throw new IllegalArgumentException("overrideRouting must not be null");
        }
        validateEntries(baseRouting, "serviceId", "channel");
        validateEntries(overrideRouting, "serviceId", "channel");

        Map<String, String> merged = new LinkedHashMap<>(baseRouting);
        merged.putAll(overrideRouting);
        return merged;
    }

    public static boolean removeServiceRouting(Map<String, String> serviceToChannel, String serviceId) {
        if (serviceToChannel == null) {
            throw new IllegalArgumentException("serviceToChannel must not be null");
        }
        validateNonBlank(serviceId, "serviceId");
        return serviceToChannel.remove(serviceId) != null;
    }

    public static List<String> routingAuditLines(Map<String, String> serviceToChannel) {
        if (serviceToChannel == null) {
            throw new IllegalArgumentException("serviceToChannel must not be null");
        }
        validateEntries(serviceToChannel, "serviceId", "channel");
        return serviceToChannel.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.toList());
    }

    public static Map<String, Long> ownerServiceCounts(Map<String, String> serviceToOwner) {
        if (serviceToOwner == null) {
            throw new IllegalArgumentException("serviceToOwner must not be null");
        }
        validateEntries(serviceToOwner, "serviceId", "owner");
        return serviceToOwner.values().stream()
            .map(String::trim)
            .map(String::toLowerCase)
            .collect(Collectors.groupingBy(
                Function.identity(),
                LinkedHashMap::new,
                Collectors.counting()
            ));
    }

    private static void validateNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must be non-blank");
        }
    }

    private static void validateEntries(Map<String, String> values, String keyName, String valueName) {
        for (Map.Entry<String, String> entry : values.entrySet()) {
            validateNonBlank(entry.getKey(), keyName);
            validateNonBlank(entry.getValue(), valueName);
        }
    }
}
