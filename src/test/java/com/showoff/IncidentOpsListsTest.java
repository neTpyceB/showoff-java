package com.showoff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class IncidentOpsListsTest {
    @Test
    void defaultEscalationChain_returnsMutableArrayList() {
        List<String> teams = IncidentOpsLists.defaultEscalationChain();

        assertIterableEquals(
            List.of("on-call-engineer", "service-owner", "incident-commander"),
            teams
        );

        teams.add("security-response");
        assertEquals(4, teams.size());
    }

    @Test
    void mergeUniqueServiceOwners_combinesListsPreservingOrder() {
        List<String> mergedOwners = IncidentOpsLists.mergeUniqueServiceOwners(
            List.of("payments-team", "identity-team"),
            List.of("platform-team", "identity-team", "sre-team")
        );

        assertIterableEquals(
            List.of("payments-team", "identity-team", "platform-team", "sre-team"),
            mergedOwners
        );
    }

    @Test
    void mergeUniqueServiceOwners_validatesInputsAndItems() {
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsLists.mergeUniqueServiceOwners(null, List.of()));
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsLists.mergeUniqueServiceOwners(List.of(), null));
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsLists.mergeUniqueServiceOwners(
                new ArrayList<>(List.of("payments-team", " ")),
                List.of("platform-team")
            )
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> {
                List<String> owners = new ArrayList<>(List.of("payments-team"));
                owners.add(null);
                IncidentOpsLists.mergeUniqueServiceOwners(owners, List.of("platform-team"));
            }
        );
    }

    @Test
    void runningErrorCounts_buildsCumulativeSums() {
        assertIterableEquals(List.of(), IncidentOpsLists.runningErrorCounts(List.of()));
        assertIterableEquals(List.of(3, 8, 15), IncidentOpsLists.runningErrorCounts(List.of(3, 5, 7)));
    }

    @Test
    void runningErrorCounts_validatesInputs() {
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsLists.runningErrorCounts(null));
        assertThrows(
            IllegalArgumentException.class,
            () -> {
                List<Integer> counts = new ArrayList<>(List.of(3, 7));
                counts.add(1, null);
                IncidentOpsLists.runningErrorCounts(counts);
            }
        );
    }

    @Test
    void immutableRunbookSnapshot_returnsIndependentUnmodifiableList() {
        List<String> runbookSteps = new ArrayList<>(List.of("detect", "triage"));
        List<String> snapshot = IncidentOpsLists.immutableRunbookSnapshot(runbookSteps);

        runbookSteps.add("mitigate");
        assertIterableEquals(List.of("detect", "triage"), snapshot);
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add("resolve"));
    }

    @Test
    void immutableRunbookSnapshot_validatesNullInputAndNullElements() {
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsLists.immutableRunbookSnapshot(null));
        assertThrows(
            NullPointerException.class,
            () -> {
                List<String> runbookSteps = new ArrayList<>(List.of("detect"));
                runbookSteps.add(null);
                IncidentOpsLists.immutableRunbookSnapshot(runbookSteps);
            }
        );
    }
}
