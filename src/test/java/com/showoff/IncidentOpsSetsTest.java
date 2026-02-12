package com.showoff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class IncidentOpsSetsTest {
    @Test
    void defaultSuppressedAlertCodes_returnsMutableSet() {
        Set<String> codes = IncidentOpsSets.defaultSuppressedAlertCodes();

        assertIterableEquals(
            List.of("HEALTHCHECK_TIMEOUT", "DEPLOYMENT_IN_PROGRESS", "READ_REPLICA_LAG"),
            new ArrayList<>(codes)
        );

        codes.add("LOW_TRAFFIC_NIGHTLY_BATCH");
        assertEquals(4, codes.size());
    }

    @Test
    void mergeUniqueResponderGroups_combinesSetsWithoutDuplicates() {
        Set<String> primary = new LinkedHashSet<>(List.of("payments-oncall", "platform-oncall"));
        Set<String> backup = new LinkedHashSet<>(List.of("security-oncall", "platform-oncall", "sre-oncall"));

        Set<String> merged = IncidentOpsSets.mergeUniqueResponderGroups(primary, backup);

        assertIterableEquals(
            List.of("payments-oncall", "platform-oncall", "security-oncall", "sre-oncall"),
            new ArrayList<>(merged)
        );
    }

    @Test
    void mergeUniqueResponderGroups_validatesInputsAndItems() {
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsSets.mergeUniqueResponderGroups(null, Set.of()));
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsSets.mergeUniqueResponderGroups(Set.of(), null));

        Set<String> withBlank = new LinkedHashSet<>();
        withBlank.add("payments-oncall");
        withBlank.add(" ");
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsSets.mergeUniqueResponderGroups(withBlank, Set.of("platform-oncall"))
        );

        Set<String> withNull = new LinkedHashSet<>();
        withNull.add("payments-oncall");
        withNull.add(null);
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsSets.mergeUniqueResponderGroups(withNull, Set.of("platform-oncall"))
        );
    }

    @Test
    void findDuplicateIncidentIds_returnsOnlyRepeatedIds() {
        Set<String> duplicates = IncidentOpsSets.findDuplicateIncidentIds(
            List.of("INC-1001", "INC-1002", "INC-1001", "INC-2000", "INC-1002", "INC-1002")
        );

        assertIterableEquals(List.of("INC-1001", "INC-1002"), new ArrayList<>(duplicates));
    }

    @Test
    void findDuplicateIncidentIds_handlesEmptyInput() {
        assertIterableEquals(List.of(), new ArrayList<>(IncidentOpsSets.findDuplicateIncidentIds(List.of())));
    }

    @Test
    void findDuplicateIncidentIds_validatesInputs() {
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsSets.findDuplicateIncidentIds(null));

        List<String> withBlank = new ArrayList<>(List.of("INC-1001", "INC-1002"));
        withBlank.add(" ");
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsSets.findDuplicateIncidentIds(withBlank));

        List<String> withNull = new ArrayList<>(List.of("INC-1001", "INC-1002"));
        withNull.add(null);
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsSets.findDuplicateIncidentIds(withNull));
    }

    @Test
    void immutableMaintenanceWindows_returnsIndependentUnmodifiableSet() {
        Set<String> windows = new LinkedHashSet<>(List.of("SUN-01:00-03:00", "WED-02:00-03:00"));
        Set<String> snapshot = IncidentOpsSets.immutableMaintenanceWindows(windows);

        windows.add("FRI-04:00-05:00");
        assertEquals(Set.of("SUN-01:00-03:00", "WED-02:00-03:00"), snapshot);
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add("SAT-04:00-05:00"));
    }

    @Test
    void immutableMaintenanceWindows_validatesInputAndElements() {
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsSets.immutableMaintenanceWindows(null));

        Set<String> withNull = new LinkedHashSet<>();
        withNull.add("SUN-01:00-03:00");
        withNull.add(null);
        assertThrows(NullPointerException.class, () -> IncidentOpsSets.immutableMaintenanceWindows(withNull));
    }

    @Test
    void sortedActiveRegions_sortsAlphabetically() {
        Set<String> regions = new LinkedHashSet<>(List.of("eu-west-1", "ap-south-1", "us-east-1"));
        assertIterableEquals(List.of("ap-south-1", "eu-west-1", "us-east-1"), IncidentOpsSets.sortedActiveRegions(regions));
    }

    @Test
    void sortedActiveRegions_validatesInput() {
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsSets.sortedActiveRegions(null));

        Set<String> withBlank = new LinkedHashSet<>(List.of("eu-west-1"));
        withBlank.add(" ");
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsSets.sortedActiveRegions(withBlank));

        Set<String> withNull = new LinkedHashSet<>(List.of("eu-west-1"));
        withNull.add(null);
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsSets.sortedActiveRegions(withNull));
    }

    @Test
    void sharedResponderGroups_andUnresolvedSuppressionCodes_coverSetOps() {
        Set<String> left = new LinkedHashSet<>(List.of("payments-oncall", "platform-oncall", "security-oncall"));
        Set<String> right = new LinkedHashSet<>(List.of("platform-oncall", "sre-oncall", "security-oncall"));
        assertIterableEquals(
            List.of("platform-oncall", "security-oncall"),
            new ArrayList<>(IncidentOpsSets.sharedResponderGroups(left, right))
        );

        Set<String> defaults = new LinkedHashSet<>(List.of("DEPLOYMENT_IN_PROGRESS", "READ_REPLICA_LAG"));
        Set<String> overrides = new LinkedHashSet<>(List.of("READ_REPLICA_LAG"));
        assertIterableEquals(
            List.of("DEPLOYMENT_IN_PROGRESS"),
            new ArrayList<>(IncidentOpsSets.unresolvedSuppressionCodes(defaults, overrides))
        );
    }

    @Test
    void sharedResponderGroups_andUnresolvedSuppressionCodes_validateInput() {
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsSets.sharedResponderGroups(null, Set.of()));
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsSets.sharedResponderGroups(Set.of(), null));

        Set<String> invalidLeft = new LinkedHashSet<>(List.of("payments-oncall"));
        invalidLeft.add(" ");
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsSets.sharedResponderGroups(invalidLeft, Set.of("platform-oncall"))
        );

        assertThrows(IllegalArgumentException.class, () -> IncidentOpsSets.unresolvedSuppressionCodes(null, Set.of()));
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsSets.unresolvedSuppressionCodes(Set.of(), null));

        Set<String> invalidOverrides = new LinkedHashSet<>(List.of("DEPLOYMENT_IN_PROGRESS"));
        invalidOverrides.add(null);
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsSets.unresolvedSuppressionCodes(Set.of("DEPLOYMENT_IN_PROGRESS"), invalidOverrides)
        );
    }

    @Test
    void normalizedRegionsByPrefix_filtersNormalizesAndSorts() {
        Set<String> regions = new LinkedHashSet<>(List.of("EU-WEST-1", " us-east-1 ", "eu-central-1", "ap-south-1"));
        assertIterableEquals(
            List.of("eu-central-1", "eu-west-1"),
            new ArrayList<>(IncidentOpsSets.normalizedRegionsByPrefix(regions, " eu-"))
        );
    }

    @Test
    void normalizedRegionsByPrefix_validatesInput() {
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsSets.normalizedRegionsByPrefix(null, "eu-"));
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsSets.normalizedRegionsByPrefix(Set.of("eu-west-1"), " ")
        );

        Set<String> withBlank = new LinkedHashSet<>(List.of("eu-west-1"));
        withBlank.add(" ");
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsSets.normalizedRegionsByPrefix(withBlank, "eu-")
        );
    }
}
