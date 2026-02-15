package com.showoff.incidentops.streams;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class IncidentOpsStreamAnalyticsTest {
    @Test
    void sortedCriticalIncidentIds_usesFilterMapSortedToList() {
        List<IncidentOpsStreamAnalytics.IncidentEvent> incidents = List.of(
            new IncidentOpsStreamAnalytics.IncidentEvent("INC-1003", "payments-api", 5, "team-payments", 1200),
            new IncidentOpsStreamAnalytics.IncidentEvent("INC-1001", "identity-api", 2, "team-identity", 80),
            new IncidentOpsStreamAnalytics.IncidentEvent("INC-1002", "payments-api", 4, "team-payments", 900)
        );

        assertIterableEquals(
            List.of("INC-1002", "INC-1003"),
            IncidentOpsStreamAnalytics.sortedCriticalIncidentIds(incidents)
        );
    }

    @Test
    void highLatencyServiceAlerts_usesSortedForEachAndToList() {
        Map<String, Integer> latencyByService = new LinkedHashMap<>();
        latencyByService.put("payments-api", 1200);
        latencyByService.put("identity-api", 300);
        latencyByService.put("search-api", 1200);

        List<String> delivered = new ArrayList<>();
        List<String> alerts = IncidentOpsStreamAnalytics.highLatencyServiceAlerts(
            latencyByService,
            500,
            delivered::add
        );

        assertIterableEquals(
            List.of(
                "ALERT service=search-api latencyMs=1200",
                "ALERT service=payments-api latencyMs=1200"
            ),
            alerts
        );
        assertIterableEquals(alerts, delivered);
        assertIterableEquals(
            List.of(),
            IncidentOpsStreamAnalytics.highLatencyServiceAlerts(new LinkedHashMap<>(), 500, msg -> {})
        );
    }

    @Test
    void incidentIdsByService_usesCollectGroupingAndMapping() {
        List<IncidentOpsStreamAnalytics.IncidentEvent> incidents = List.of(
            new IncidentOpsStreamAnalytics.IncidentEvent("INC-2001", "payments-api", 4, "team-payments", 900),
            new IncidentOpsStreamAnalytics.IncidentEvent("INC-2002", "identity-api", 3, "team-identity", 200),
            new IncidentOpsStreamAnalytics.IncidentEvent("INC-2003", "payments-api", 2, "team-payments", 100)
        );

        Map<String, List<String>> grouped = IncidentOpsStreamAnalytics.incidentIdsByService(incidents);
        assertEquals(
            Map.of(
                "payments-api", List.of("INC-2001", "INC-2003"),
                "identity-api", List.of("INC-2002")
            ),
            grouped
        );
    }

    @Test
    void incidentCountBySeverity_usesGroupingAndCounting() {
        List<IncidentOpsStreamAnalytics.IncidentEvent> incidents = List.of(
            new IncidentOpsStreamAnalytics.IncidentEvent("INC-3001", "payments-api", 4, "team-payments", 900),
            new IncidentOpsStreamAnalytics.IncidentEvent("INC-3002", "identity-api", 4, "team-identity", 200),
            new IncidentOpsStreamAnalytics.IncidentEvent("INC-3003", "search-api", 2, "team-search", 100)
        );

        Map<Integer, Long> counts = IncidentOpsStreamAnalytics.incidentCountBySeverity(incidents);
        assertEquals(Map.of(4, 2L, 2, 1L), counts);
    }

    @Test
    void ownerTeamsAlphabetical_usesMappingDistinctAndCollecting() {
        List<IncidentOpsStreamAnalytics.IncidentEvent> incidents = List.of(
            new IncidentOpsStreamAnalytics.IncidentEvent("INC-4001", "payments-api", 4, " Team-Payments ", 900),
            new IncidentOpsStreamAnalytics.IncidentEvent("INC-4002", "identity-api", 2, "team-identity", 200),
            new IncidentOpsStreamAnalytics.IncidentEvent("INC-4003", "search-api", 1, "TEAM-PAYMENTS", 100)
        );
        assertIterableEquals(
            List.of("team-identity", "team-payments"),
            IncidentOpsStreamAnalytics.ownerTeamsAlphabetical(incidents)
        );
    }

    @Test
    void totalLatencyMs_usesReduce() {
        List<IncidentOpsStreamAnalytics.IncidentEvent> incidents = List.of(
            new IncidentOpsStreamAnalytics.IncidentEvent("INC-5001", "payments-api", 4, "team-payments", 900),
            new IncidentOpsStreamAnalytics.IncidentEvent("INC-5002", "identity-api", 2, "team-identity", 200)
        );
        assertEquals(1100, IncidentOpsStreamAnalytics.totalLatencyMs(incidents));
        assertEquals(0, IncidentOpsStreamAnalytics.totalLatencyMs(List.of()));
    }

    @Test
    void slowestServiceId_andSortServicesByLatency_useComparatorAndReverse() {
        Map<String, Integer> latencyByService = new LinkedHashMap<>();
        latencyByService.put("payments-api", 900);
        latencyByService.put("identity-api", 1200);
        latencyByService.put("search-api", 1200);

        Optional<String> max = IncidentOpsStreamAnalytics.slowestServiceId(latencyByService);
        assertEquals(Optional.of("search-api"), max);

        assertIterableEquals(
            List.of("payments-api", "identity-api", "search-api"),
            IncidentOpsStreamAnalytics.sortServicesByLatency(latencyByService, false)
        );
        assertIterableEquals(
            List.of("search-api", "identity-api", "payments-api"),
            IncidentOpsStreamAnalytics.sortServicesByLatency(latencyByService, true)
        );
        assertFalse(IncidentOpsStreamAnalytics.slowestServiceId(new LinkedHashMap<>()).isPresent());
    }

    @Test
    void streamAnalytics_validatesIncidentInputs() {
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsStreamAnalytics.sortedCriticalIncidentIds(null));
        assertThrows(
            IllegalArgumentException.class,
            () -> {
                List<IncidentOpsStreamAnalytics.IncidentEvent> incidents = new ArrayList<>();
                incidents.add(null);
                IncidentOpsStreamAnalytics.sortedCriticalIncidentIds(incidents);
            }
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsStreamAnalytics.sortedCriticalIncidentIds(
                List.of(new IncidentOpsStreamAnalytics.IncidentEvent(" ", "payments-api", 3, "team-payments", 1))
            )
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsStreamAnalytics.sortedCriticalIncidentIds(
                List.of(new IncidentOpsStreamAnalytics.IncidentEvent("INC-1", " ", 3, "team-payments", 1))
            )
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsStreamAnalytics.sortedCriticalIncidentIds(
                List.of(new IncidentOpsStreamAnalytics.IncidentEvent("INC-1", "payments-api", 3, " ", 1))
            )
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsStreamAnalytics.sortedCriticalIncidentIds(
                List.of(new IncidentOpsStreamAnalytics.IncidentEvent("INC-1", "payments-api", 0, "team-payments", 1))
            )
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsStreamAnalytics.sortedCriticalIncidentIds(
                List.of(new IncidentOpsStreamAnalytics.IncidentEvent("INC-1", "payments-api", 3, "team-payments", -1))
            )
        );
    }

    @Test
    void streamAnalytics_validatesLatencyMapAndAlertArguments() {
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsStreamAnalytics.sortServicesByLatency(null, false)
        );

        Map<String, Integer> withBlankKey = new LinkedHashMap<>();
        withBlankKey.put(" ", 10);
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsStreamAnalytics.sortServicesByLatency(withBlankKey, false)
        );

        Map<String, Integer> withNullLatency = new LinkedHashMap<>();
        withNullLatency.put("payments-api", null);
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsStreamAnalytics.sortServicesByLatency(withNullLatency, false)
        );

        Map<String, Integer> withNegativeLatency = new LinkedHashMap<>();
        withNegativeLatency.put("payments-api", -1);
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsStreamAnalytics.sortServicesByLatency(withNegativeLatency, false)
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsStreamAnalytics.highLatencyServiceAlerts(new LinkedHashMap<>(), -1, msg -> {})
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsStreamAnalytics.highLatencyServiceAlerts(new LinkedHashMap<>(), 10, null)
        );
        assertTrue(IncidentOpsStreamAnalytics.highLatencyServiceAlerts(new LinkedHashMap<>(), 0, msg -> {}).isEmpty());
    }
}
