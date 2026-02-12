package com.showoff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class IncidentOpsOptionalsTest {
    @Test
    void findPagerChannel_returnsOptionalPresentOrEmpty() {
        Map<String, String> routing = new LinkedHashMap<>();
        routing.put("payments-api", "#payments");

        assertEquals(Optional.of("#payments"), IncidentOpsOptionals.findPagerChannel(routing, "payments-api"));
        assertEquals(Optional.empty(), IncidentOpsOptionals.findPagerChannel(routing, "identity-api"));
    }

    @Test
    void findPagerChannel_validatesInput() {
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsOptionals.findPagerChannel(null, "payments-api"));
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsOptionals.findPagerChannel(new LinkedHashMap<>(), " ")
        );
    }

    @Test
    void pagerChannelOrDefault_usesMapFilterAndOrElse() {
        assertEquals(
            "#payments",
            IncidentOpsOptionals.pagerChannelOrDefault(Optional.of(" #PAYMENTS "), "#incident-command")
        );
        assertEquals(
            "#incident-command",
            IncidentOpsOptionals.pagerChannelOrDefault(Optional.of(" "), "#incident-command")
        );
        assertEquals(
            "#incident-command",
            IncidentOpsOptionals.pagerChannelOrDefault(Optional.empty(), "#incident-command")
        );
    }

    @Test
    void pagerChannelOrDefault_validatesInput() {
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsOptionals.pagerChannelOrDefault(null, "#incident-command")
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsOptionals.pagerChannelOrDefault(Optional.empty(), " ")
        );
    }

    @Test
    void pagerChannelOrComputedDefault_usesOrElseGet() {
        assertEquals(
            "#payments",
            IncidentOpsOptionals.pagerChannelOrComputedDefault(Optional.of("#payments"), "payments-api")
        );
        assertEquals(
            "#alerts-payments-api",
            IncidentOpsOptionals.pagerChannelOrComputedDefault(Optional.empty(), "Payments-Api")
        );
    }

    @Test
    void pagerChannelOrComputedDefault_validatesInput() {
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsOptionals.pagerChannelOrComputedDefault(null, "payments-api")
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsOptionals.pagerChannelOrComputedDefault(Optional.empty(), " ")
        );
    }

    @Test
    void pagerChannelOrThrow_usesOrElseThrow() {
        assertEquals(
            "#payments",
            IncidentOpsOptionals.pagerChannelOrThrow(Optional.of(" #payments "), "payments-api")
        );
        assertThrows(
            IllegalStateException.class,
            () -> IncidentOpsOptionals.pagerChannelOrThrow(Optional.empty(), "payments-api")
        );
        assertThrows(
            IllegalStateException.class,
            () -> IncidentOpsOptionals.pagerChannelOrThrow(Optional.of(" "), "payments-api")
        );
    }

    @Test
    void pagerChannelOrThrow_validatesInput() {
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsOptionals.pagerChannelOrThrow(null, "payments-api")
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsOptionals.pagerChannelOrThrow(Optional.empty(), " ")
        );
    }

    @Test
    void effectiveRunbook_usesOrBetweenOptionals() {
        assertEquals(
            Optional.of("runbook-override"),
            IncidentOpsOptionals.effectiveRunbook(Optional.of(" runbook-override "), Optional.of("runbook-default"))
        );
        assertEquals(
            Optional.of("runbook-default"),
            IncidentOpsOptionals.effectiveRunbook(Optional.of(" "), Optional.of(" runbook-default "))
        );
        assertEquals(
            Optional.empty(),
            IncidentOpsOptionals.effectiveRunbook(Optional.empty(), Optional.of(" "))
        );
    }

    @Test
    void effectiveRunbook_validatesInput() {
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsOptionals.effectiveRunbook(null, Optional.empty())
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsOptionals.effectiveRunbook(Optional.empty(), null)
        );
    }

    @Test
    void incidentAcknowledgementMessage_usesIfPresentOrElse() {
        assertEquals(
            "acknowledged INC-7001",
            IncidentOpsOptionals.incidentAcknowledgementMessage(Optional.of(" INC-7001 "))
        );
        assertEquals(
            "no incident to acknowledge",
            IncidentOpsOptionals.incidentAcknowledgementMessage(Optional.empty())
        );
        assertEquals(
            "no incident to acknowledge",
            IncidentOpsOptionals.incidentAcknowledgementMessage(Optional.of(" "))
        );
    }

    @Test
    void incidentAcknowledgementMessage_validatesInput() {
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsOptionals.incidentAcknowledgementMessage(null));
    }

    @Test
    void buildNotificationTargets_usesOptionalStreamAndIfPresent() {
        List<String> ownerChannels = new ArrayList<>(List.of(" #Payments ", "#platform", "#payments"));
        assertEquals(
            List.of("#payments", "#platform"),
            IncidentOpsOptionals.buildNotificationTargets(Optional.of(ownerChannels), Optional.of("#incident-command"))
        );

        assertEquals(
            List.of("#incident-command"),
            IncidentOpsOptionals.buildNotificationTargets(Optional.empty(), Optional.of(" #INCIDENT-COMMAND "))
        );

        assertEquals(
            List.of(),
            IncidentOpsOptionals.buildNotificationTargets(Optional.empty(), Optional.empty())
        );
    }

    @Test
    void buildNotificationTargets_validatesInput() {
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsOptionals.buildNotificationTargets(null, Optional.empty())
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsOptionals.buildNotificationTargets(Optional.empty(), null)
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsOptionals.buildNotificationTargets(Optional.empty(), Optional.of(" "))
        );
    }

    @Test
    void parseSlaMinutes_usesFlatMapAndTryCatchParser() {
        assertEquals(Optional.of(15), IncidentOpsOptionals.parseSlaMinutes(Optional.of(" 15 ")));
        assertEquals(Optional.empty(), IncidentOpsOptionals.parseSlaMinutes(Optional.of("0")));
        assertEquals(Optional.empty(), IncidentOpsOptionals.parseSlaMinutes(Optional.of("-3")));
        assertEquals(Optional.empty(), IncidentOpsOptionals.parseSlaMinutes(Optional.of("not-int")));
        assertEquals(Optional.empty(), IncidentOpsOptionals.parseSlaMinutes(Optional.of(" ")));
        assertEquals(Optional.empty(), IncidentOpsOptionals.parseSlaMinutes(Optional.empty()));
    }

    @Test
    void parseSlaMinutes_validatesInput() {
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsOptionals.parseSlaMinutes(null));
    }
}
