package com.showoff;

import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class IncidentOpsSets {
    private IncidentOpsSets() {}

    public static Set<String> defaultSuppressedAlertCodes() {
        Set<String> codes = new LinkedHashSet<>();
        codes.add("HEALTHCHECK_TIMEOUT");
        codes.add("DEPLOYMENT_IN_PROGRESS");
        codes.add("READ_REPLICA_LAG");
        return codes;
    }

    public static Set<String> mergeUniqueResponderGroups(Set<String> primaryGroups, Set<String> backupGroups) {
        if (primaryGroups == null) {
            throw new IllegalArgumentException("primaryGroups must not be null");
        }
        if (backupGroups == null) {
            throw new IllegalArgumentException("backupGroups must not be null");
        }

        Set<String> merged = new LinkedHashSet<>(primaryGroups.size() + backupGroups.size());
        appendValidated(merged, primaryGroups);
        appendValidated(merged, backupGroups);
        return merged;
    }

    public static Set<String> findDuplicateIncidentIds(List<String> incidentIds) {
        if (incidentIds == null) {
            throw new IllegalArgumentException("incidentIds must not be null");
        }

        Set<String> seen = new LinkedHashSet<>(incidentIds.size());
        Set<String> duplicates = new LinkedHashSet<>();
        for (String incidentId : incidentIds) {
            if (incidentId == null || incidentId.isBlank()) {
                throw new IllegalArgumentException("incident id must be non-blank");
            }
            if (!seen.add(incidentId)) {
                duplicates.add(incidentId);
            }
        }
        return duplicates;
    }

    public static Set<String> immutableMaintenanceWindows(Set<String> windows) {
        if (windows == null) {
            throw new IllegalArgumentException("windows must not be null");
        }
        return Set.copyOf(windows);
    }

    public static List<String> sortedActiveRegions(Set<String> regions) {
        if (regions == null) {
            throw new IllegalArgumentException("regions must not be null");
        }
        List<String> sorted = new ArrayList<>(regions.size());
        for (String region : regions) {
            validateNonBlank(region, "region");
            sorted.add(region);
        }
        sorted.sort(String::compareTo);
        return sorted;
    }

    public static Set<String> sharedResponderGroups(Set<String> left, Set<String> right) {
        if (left == null) {
            throw new IllegalArgumentException("left must not be null");
        }
        if (right == null) {
            throw new IllegalArgumentException("right must not be null");
        }
        validateAll(left, "group");
        validateAll(right, "group");
        Set<String> shared = new LinkedHashSet<>(left);
        shared.retainAll(right);
        return shared;
    }

    public static Set<String> unresolvedSuppressionCodes(Set<String> defaults, Set<String> overrides) {
        if (defaults == null) {
            throw new IllegalArgumentException("defaults must not be null");
        }
        if (overrides == null) {
            throw new IllegalArgumentException("overrides must not be null");
        }
        validateAll(defaults, "code");
        validateAll(overrides, "code");
        Set<String> unresolved = new LinkedHashSet<>(defaults);
        unresolved.removeAll(overrides);
        return unresolved;
    }

    public static Set<String> normalizedRegionsByPrefix(Set<String> regions, String prefix) {
        if (regions == null) {
            throw new IllegalArgumentException("regions must not be null");
        }
        validateNonBlank(prefix, "prefix");
        for (String region : regions) {
            validateNonBlank(region, "region");
        }
        String normalizedPrefix = prefix.trim().toLowerCase();
        return regions.stream()
            .map(String::trim)
            .map(String::toLowerCase)
            .filter(region -> region.startsWith(normalizedPrefix))
            .sorted()
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static void appendValidated(Set<String> target, Set<String> source) {
        for (String group : source) {
            validateNonBlank(group, "group");
            target.add(group);
        }
    }

    private static void validateAll(Set<String> values, String fieldName) {
        for (String value : values) {
            validateNonBlank(value, fieldName);
        }
    }

    private static void validateNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must be non-blank");
        }
    }
}
