package com.showoff;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

    private static void appendValidated(Set<String> target, Set<String> source) {
        for (String group : source) {
            if (group == null || group.isBlank()) {
                throw new IllegalArgumentException("group id must be non-blank");
            }
            target.add(group);
        }
    }
}
