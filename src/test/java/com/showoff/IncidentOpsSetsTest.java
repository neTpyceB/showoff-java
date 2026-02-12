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
}
