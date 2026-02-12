package com.showoff;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

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

    public static List<String> sortedUniqueRunbookSteps(List<String> runbookSteps) {
        if (runbookSteps == null) {
            throw new IllegalArgumentException("runbookSteps must not be null");
        }
        LinkedHashSet<String> unique = new LinkedHashSet<>(runbookSteps.size());
        for (String step : runbookSteps) {
            validateNonBlank(step, "runbookStep");
            unique.add(step);
        }
        List<String> sorted = new ArrayList<>(unique);
        sorted.sort(String::compareTo);
        return sorted;
    }

    public static void sortIncidentSeveritiesDescending(List<Integer> severities) {
        if (severities == null) {
            throw new IllegalArgumentException("severities must not be null");
        }
        for (Integer severity : severities) {
            if (severity == null) {
                throw new IllegalArgumentException("severities must not contain null");
            }
        }
        severities.sort((a, b) -> Integer.compare(b, a));
    }

    public static List<String> firstResponderWindow(List<String> escalationChain, int windowSize) {
        if (escalationChain == null) {
            throw new IllegalArgumentException("escalationChain must not be null");
        }
        if (windowSize <= 0) {
            throw new IllegalArgumentException("windowSize must be > 0");
        }
        for (String team : escalationChain) {
            validateNonBlank(team, "team");
        }
        int end = Math.min(windowSize, escalationChain.size());
        return new ArrayList<>(escalationChain.subList(0, end));
    }

    public static List<String> normalizedOwnersByPriority(List<String> ownerIds) {
        if (ownerIds == null) {
            throw new IllegalArgumentException("ownerIds must not be null");
        }
        for (String ownerId : ownerIds) {
            validateNonBlank(ownerId, "ownerId");
        }
        return ownerIds.stream()
            .map(String::trim)
            .map(String::toLowerCase)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }

    private static void appendIfMissing(List<String> target, List<String> source) {
        for (String item : source) {
            validateNonBlank(item, "ownerId");
            if (!target.contains(item)) {
                target.add(item);
            }
        }
    }

    private static void validateNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must be non-blank");
        }
    }
}
