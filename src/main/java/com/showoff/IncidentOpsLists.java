package com.showoff;

import java.util.ArrayList;
import java.util.List;

public final class IncidentOpsLists {
    private IncidentOpsLists() {}

    public static List<String> defaultEscalationChain() {
        List<String> teams = new ArrayList<>();
        teams.add("on-call-engineer");
        teams.add("service-owner");
        teams.add("incident-commander");
        return teams;
    }

    public static List<String> mergeUniqueServiceOwners(List<String> primaryOwners, List<String> backupOwners) {
        if (primaryOwners == null) {
            throw new IllegalArgumentException("primaryOwners must not be null");
        }
        if (backupOwners == null) {
            throw new IllegalArgumentException("backupOwners must not be null");
        }
        List<String> mergedOwners = new ArrayList<>(primaryOwners.size() + backupOwners.size());
        appendIfMissing(mergedOwners, primaryOwners);
        appendIfMissing(mergedOwners, backupOwners);
        return mergedOwners;
    }

    public static List<Integer> runningErrorCounts(List<Integer> hourlyErrors) {
        if (hourlyErrors == null) {
            throw new IllegalArgumentException("hourlyErrors must not be null");
        }
        List<Integer> cumulative = new ArrayList<>(hourlyErrors.size());
        int total = 0;
        for (Integer count : hourlyErrors) {
            if (count == null) {
                throw new IllegalArgumentException("hourlyErrors must not contain null");
            }
            total += count;
            cumulative.add(total);
        }
        return cumulative;
    }

    public static List<String> immutableRunbookSnapshot(List<String> runbookSteps) {
        if (runbookSteps == null) {
            throw new IllegalArgumentException("runbookSteps must not be null");
        }
        return List.copyOf(runbookSteps);
    }

    private static void appendIfMissing(List<String> target, List<String> source) {
        for (String item : source) {
            if (item == null || item.isBlank()) {
                throw new IllegalArgumentException("owner id must be non-blank");
            }
            if (!target.contains(item)) {
                target.add(item);
            }
        }
    }
}
