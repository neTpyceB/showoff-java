package com.showoff.incidentops.functional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class IncidentOpsFunctionalWorkflowsTest {
    @Test
    void transformServiceIds_usesFunctionLambda() {
        List<String> transformed = IncidentOpsFunctionalWorkflows.transformServiceIds(
            List.of("Payments-Api", "Identity-Api"),
            value -> value.trim().toLowerCase()
        );
        assertIterableEquals(List.of("payments-api", "identity-api"), transformed);
    }

    @Test
    void transformServiceIds_validatesInputs() {
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsFunctionalWorkflows.transformServiceIds(null, value -> value)
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsFunctionalWorkflows.transformServiceIds(List.of("payments-api"), null)
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsFunctionalWorkflows.transformServiceIds(new ArrayList<>(List.of(" ")), value -> value)
        );
        List<String> withNull = new ArrayList<>();
        withNull.add(null);
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsFunctionalWorkflows.transformServiceIds(withNull, value -> value)
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsFunctionalWorkflows.transformServiceIds(List.of("payments-api"), value -> " ")
        );
    }

    @Test
    void selectEscalationCandidates_usesPredicateLambda() {
        List<String> selected = IncidentOpsFunctionalWorkflows.selectEscalationCandidates(
            List.of("INC-1001", "MAINT-2001", "INC-1002"),
            incidentId -> incidentId.startsWith("INC-")
        );
        assertIterableEquals(List.of("INC-1001", "INC-1002"), selected);
    }

    @Test
    void selectEscalationCandidates_validatesInputs() {
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsFunctionalWorkflows.selectEscalationCandidates(null, incidentId -> true)
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsFunctionalWorkflows.selectEscalationCandidates(List.of("INC-1001"), null)
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsFunctionalWorkflows.selectEscalationCandidates(new ArrayList<>(List.of(" ")), incidentId -> true)
        );
        List<String> withNull = new ArrayList<>();
        withNull.add(null);
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsFunctionalWorkflows.selectEscalationCandidates(withNull, incidentId -> true)
        );
    }

    @Test
    void sendPagerNotifications_usesConsumerLambda() {
        List<String> sent = new ArrayList<>();
        int count = IncidentOpsFunctionalWorkflows.sendPagerNotifications(
            List.of("#payments", "#identity"),
            sent::add
        );
        assertEquals(2, count);
        assertIterableEquals(List.of("#payments", "#identity"), sent);
    }

    @Test
    void sendPagerNotifications_validatesInputs() {
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsFunctionalWorkflows.sendPagerNotifications(null, channel -> {})
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsFunctionalWorkflows.sendPagerNotifications(List.of("#payments"), null)
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsFunctionalWorkflows.sendPagerNotifications(new ArrayList<>(List.of(" ")), channel -> {})
        );
        List<String> withNull = new ArrayList<>();
        withNull.add(null);
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsFunctionalWorkflows.sendPagerNotifications(withNull, channel -> {})
        );
    }

    @Test
    void resolveFallbackChannel_usesSupplierLambda() {
        assertEquals(
            "#incident-command",
            IncidentOpsFunctionalWorkflows.resolveFallbackChannel(() -> " #INCIDENT-COMMAND ")
        );
    }

    @Test
    void resolveFallbackChannel_validatesInputs() {
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsFunctionalWorkflows.resolveFallbackChannel(null)
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsFunctionalWorkflows.resolveFallbackChannel(() -> " ")
        );
    }

    @Test
    void buildRoutingTable_usesCustomFunctionalInterfaceLambda() {
        Map<String, Integer> rates = new LinkedHashMap<>();
        rates.put("payments-api", 12);
        rates.put("identity-api", 3);

        Map<String, String> routing = IncidentOpsFunctionalWorkflows.buildRoutingTable(
            rates,
            (serviceId, errorRate) -> errorRate >= 10 ? "#critical-" + serviceId : "#normal-" + serviceId
        );

        assertEquals(
            Map.of(
                "payments-api", "#critical-payments-api",
                "identity-api", "#normal-identity-api"
            ),
            routing
        );
    }

    @Test
    void buildRoutingTable_validatesInputsAndValues() {
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsFunctionalWorkflows.buildRoutingTable(null, (serviceId, errorRate) -> "#default")
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsFunctionalWorkflows.buildRoutingTable(new LinkedHashMap<>(), null)
        );

        Map<String, Integer> withBlankService = new LinkedHashMap<>();
        withBlankService.put(" ", 1);
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsFunctionalWorkflows.buildRoutingTable(withBlankService, (serviceId, errorRate) -> "#default")
        );

        Map<String, Integer> withNegativeRate = new LinkedHashMap<>();
        withNegativeRate.put("payments-api", -1);
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsFunctionalWorkflows.buildRoutingTable(withNegativeRate, (serviceId, errorRate) -> "#default")
        );

        Map<String, Integer> withNullRate = new LinkedHashMap<>();
        withNullRate.put("payments-api", null);
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsFunctionalWorkflows.buildRoutingTable(withNullRate, (serviceId, errorRate) -> "#default")
        );

        Map<String, Integer> valid = new LinkedHashMap<>();
        valid.put("payments-api", 1);
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsFunctionalWorkflows.buildRoutingTable(valid, (serviceId, errorRate) -> " ")
        );
    }

    @Test
    void routingStrategy_defaultMethod_canBeUsed() {
        IncidentOpsFunctionalWorkflows.RoutingStrategy strategy = (serviceId, errorRate) -> "#alerts-" + serviceId;
        assertEquals("#alerts-payments-api", strategy.routeNormalized(" Payments-Api ", 4));
    }
}
