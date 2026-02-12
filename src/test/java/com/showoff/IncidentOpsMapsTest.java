package com.showoff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class IncidentOpsMapsTest {
    @Test
    void defaultSloTargets_returnsMutableLinkedHashMap() {
        Map<String, Integer> targets = IncidentOpsMaps.defaultSloTargets();

        assertEquals(3, targets.size());
        assertEquals(99, targets.get("payments-api"));
        assertEquals(99, targets.get("identity-api"));
        assertEquals(98, targets.get("checkout-api"));

        targets.put("search-api", 99);
        assertEquals(4, targets.size());
    }

    @Test
    void upsertSloTarget_insertsAndUpdates() {
        Map<String, Integer> targets = new LinkedHashMap<>();

        assertEquals(null, IncidentOpsMaps.upsertSloTarget(targets, "payments-api", 99));
        assertEquals(99, targets.get("payments-api"));

        assertEquals(99, IncidentOpsMaps.upsertSloTarget(targets, "payments-api", 98));
        assertEquals(98, targets.get("payments-api"));
    }

    @Test
    void upsertSloTarget_validatesInput() {
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsMaps.upsertSloTarget(null, "payments-api", 99));
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsMaps.upsertSloTarget(new LinkedHashMap<>(), "payments-api", 0)
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsMaps.upsertSloTarget(new LinkedHashMap<>(), "payments-api", 101)
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsMaps.upsertSloTarget(new LinkedHashMap<>(), " ", 99)
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsMaps.upsertSloTarget(new LinkedHashMap<>(), null, 99)
        );
    }

    @Test
    void findPagerChannel_returnsExactChannelOrDefault() {
        Map<String, String> routing = new LinkedHashMap<>();
        routing.put("payments-api", "#payments-incidents");

        assertEquals("#payments-incidents", IncidentOpsMaps.findPagerChannel(routing, "payments-api"));
        assertEquals("#incident-command", IncidentOpsMaps.findPagerChannel(routing, "unknown-service"));
    }

    @Test
    void findPagerChannel_validatesInput() {
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsMaps.findPagerChannel(null, "payments-api"));
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsMaps.findPagerChannel(new LinkedHashMap<>(), " ")
        );
    }

    @Test
    void addSuppressionTagIfMissing_behavesForNewAndExistingIncident() {
        Map<String, String> tags = new LinkedHashMap<>();

        assertTrue(IncidentOpsMaps.addSuppressionTagIfMissing(tags, "INC-5001", "deployment-window"));
        assertEquals("deployment-window", tags.get("INC-5001"));

        assertEquals(false, IncidentOpsMaps.addSuppressionTagIfMissing(tags, "INC-5001", "ignore"));
        assertEquals("deployment-window", tags.get("INC-5001"));
    }

    @Test
    void addSuppressionTagIfMissing_validatesInput() {
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsMaps.addSuppressionTagIfMissing(null, "INC-5001", "deployment-window")
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsMaps.addSuppressionTagIfMissing(new LinkedHashMap<>(), " ", "deployment-window")
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsMaps.addSuppressionTagIfMissing(new LinkedHashMap<>(), "INC-5001", " ")
        );
    }

    @Test
    void incrementErrorBudgetBurn_countsViaMerge() {
        Map<String, Integer> burn = new LinkedHashMap<>();

        assertEquals(1, IncidentOpsMaps.incrementErrorBudgetBurn(burn, "payments-api"));
        assertEquals(2, IncidentOpsMaps.incrementErrorBudgetBurn(burn, "payments-api"));
        assertEquals(1, IncidentOpsMaps.incrementErrorBudgetBurn(burn, "identity-api"));
        assertEquals(2, burn.size());
    }

    @Test
    void incrementErrorBudgetBurn_validatesInput() {
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsMaps.incrementErrorBudgetBurn(null, "payments-api"));
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsMaps.incrementErrorBudgetBurn(new LinkedHashMap<>(), " ")
        );
    }

    @Test
    void timelineForIncident_usesComputeIfAbsentAndAppendsEvents() {
        Map<String, List<String>> timelineByIncident = new LinkedHashMap<>();

        List<String> first = IncidentOpsMaps.timelineForIncident(timelineByIncident, "INC-5001", "detected");
        List<String> second = IncidentOpsMaps.timelineForIncident(timelineByIncident, "INC-5001", "mitigated");

        assertEquals(first, second);
        assertIterableEquals(List.of("detected", "mitigated"), second);
    }

    @Test
    void timelineForIncident_validatesInput() {
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsMaps.timelineForIncident(null, "INC-5001", "detected")
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsMaps.timelineForIncident(new LinkedHashMap<>(), " ", "detected")
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsMaps.timelineForIncident(new LinkedHashMap<>(), "INC-5001", " ")
        );
    }

    @Test
    void normalizeStatuses_uppercasesAndTrimsValues() {
        Map<String, String> statuses = new LinkedHashMap<>();
        statuses.put("INC-5001", " open ");
        statuses.put("INC-5002", "closed");

        IncidentOpsMaps.normalizeStatuses(statuses);

        assertEquals("OPEN", statuses.get("INC-5001"));
        assertEquals("CLOSED", statuses.get("INC-5002"));
    }

    @Test
    void normalizeStatuses_validatesInputAndEntries() {
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsMaps.normalizeStatuses(null));

        Map<String, String> invalidKey = new LinkedHashMap<>();
        invalidKey.put(" ", "OPEN");
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsMaps.normalizeStatuses(invalidKey));

        Map<String, String> invalidValue = new LinkedHashMap<>();
        invalidValue.put("INC-5001", " ");
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsMaps.normalizeStatuses(invalidValue));
    }

    @Test
    void removeClosedIncidents_removesClosedAndReturnsCount() {
        Map<String, String> statuses = new LinkedHashMap<>();
        statuses.put("INC-5001", "OPEN");
        statuses.put("INC-5002", "CLOSED");
        statuses.put("INC-5003", "CLOSED");

        assertEquals(2, IncidentOpsMaps.removeClosedIncidents(statuses));
        assertEquals(1, statuses.size());
        assertEquals("OPEN", statuses.get("INC-5001"));
    }

    @Test
    void removeClosedIncidents_handlesNoMatchesAndValidatesInput() {
        Map<String, String> statuses = new LinkedHashMap<>();
        statuses.put("INC-5001", "OPEN");
        assertEquals(0, IncidentOpsMaps.removeClosedIncidents(statuses));
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsMaps.removeClosedIncidents(null));
    }

    @Test
    void summarizeOwnership_joinsEntriesInMapOrder() {
        Map<String, String> ownership = new LinkedHashMap<>();
        ownership.put("payments-api", "team-payments");
        ownership.put("identity-api", "team-identity");

        assertEquals(
            "payments-api->team-payments | identity-api->team-identity",
            IncidentOpsMaps.summarizeOwnership(ownership)
        );
        assertEquals("", IncidentOpsMaps.summarizeOwnership(new LinkedHashMap<>()));
    }

    @Test
    void summarizeOwnership_validatesInputAndEntries() {
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsMaps.summarizeOwnership(null));

        Map<String, String> invalidKey = new LinkedHashMap<>();
        invalidKey.put(" ", "team-payments");
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsMaps.summarizeOwnership(invalidKey));

        Map<String, String> invalidValue = new LinkedHashMap<>();
        invalidValue.put("payments-api", " ");
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsMaps.summarizeOwnership(invalidValue));
    }

    @Test
    void immutableRoutingSnapshot_returnsIndependentUnmodifiableMap() {
        Map<String, String> routing = new LinkedHashMap<>();
        routing.put("payments-api", "#payments-incidents");
        routing.put("identity-api", "#identity-incidents");

        Map<String, String> snapshot = IncidentOpsMaps.immutableRoutingSnapshot(routing);

        routing.put("search-api", "#search-incidents");
        assertEquals(2, snapshot.size());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.put("checkout-api", "#checkout-incidents"));
    }

    @Test
    void immutableRoutingSnapshot_validatesInputAndElements() {
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsMaps.immutableRoutingSnapshot(null));

        Map<String, String> invalidValue = new LinkedHashMap<>();
        invalidValue.put("payments-api", null);
        assertThrows(NullPointerException.class, () -> IncidentOpsMaps.immutableRoutingSnapshot(invalidValue));

        Map<String, String> invalidKey = new LinkedHashMap<>();
        invalidKey.put(null, "#payments-incidents");
        assertThrows(NullPointerException.class, () -> IncidentOpsMaps.immutableRoutingSnapshot(invalidKey));
    }
}
