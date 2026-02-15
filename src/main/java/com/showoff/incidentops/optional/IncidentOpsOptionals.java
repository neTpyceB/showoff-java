package com.showoff.incidentops.optional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class IncidentOpsOptionals {
    private IncidentOpsOptionals() {}

    public static Optional<String> findPagerChannel(Map<String, String> serviceToChannel, String serviceId) {
        if (serviceToChannel == null) {
            throw new IllegalArgumentException("serviceToChannel must not be null");
        }
        validateNonBlank(serviceId, "serviceId");
        return Optional.ofNullable(serviceToChannel.get(serviceId));
    }

    public static String pagerChannelOrDefault(Optional<String> channel, String fallback) {
        if (channel == null) {
            throw new IllegalArgumentException("channel must not be null");
        }
        validateNonBlank(fallback, "fallback");
        return channel
            .map(String::trim)
            .filter(value -> !value.isBlank())
            .map(String::toLowerCase)
            .orElse(fallback);
    }

    public static String pagerChannelOrComputedDefault(Optional<String> channel, String serviceId) {
        if (channel == null) {
            throw new IllegalArgumentException("channel must not be null");
        }
        validateNonBlank(serviceId, "serviceId");
        return channel
            .map(String::trim)
            .filter(value -> !value.isBlank())
            .orElseGet(() -> "#alerts-" + serviceId.toLowerCase());
    }

    public static String pagerChannelOrThrow(Optional<String> channel, String serviceId) {
        if (channel == null) {
            throw new IllegalArgumentException("channel must not be null");
        }
        validateNonBlank(serviceId, "serviceId");
        return channel
            .map(String::trim)
            .filter(value -> !value.isBlank())
            .orElseThrow(() -> new IllegalStateException("missing pager channel for " + serviceId));
    }

    public static Optional<String> effectiveRunbook(Optional<String> overrideRunbook, Optional<String> defaultRunbook) {
        if (overrideRunbook == null) {
            throw new IllegalArgumentException("overrideRunbook must not be null");
        }
        if (defaultRunbook == null) {
            throw new IllegalArgumentException("defaultRunbook must not be null");
        }
        return overrideRunbook
            .map(String::trim)
            .filter(value -> !value.isBlank())
            .or(() -> defaultRunbook.map(String::trim).filter(value -> !value.isBlank()));
    }

    public static String incidentAcknowledgementMessage(Optional<String> incidentId) {
        if (incidentId == null) {
            throw new IllegalArgumentException("incidentId must not be null");
        }
        StringBuilder message = new StringBuilder();
        incidentId
            .map(String::trim)
            .filter(value -> !value.isBlank())
            .ifPresentOrElse(
                id -> message.append("acknowledged ").append(id),
                () -> message.append("no incident to acknowledge")
            );
        return message.toString();
    }

    public static List<String> buildNotificationTargets(Optional<List<String>> ownerChannels, Optional<String> fallbackChannel) {
        if (ownerChannels == null) {
            throw new IllegalArgumentException("ownerChannels must not be null");
        }
        if (fallbackChannel == null) {
            throw new IllegalArgumentException("fallbackChannel must not be null");
        }
        fallbackChannel.ifPresent(channel -> validateNonBlank(channel, "fallbackChannel"));

        List<String> targets = new ArrayList<>();
        ownerChannels.stream()
            .flatMap(List::stream)
            .map(String::trim)
            .filter(value -> !value.isBlank())
            .map(String::toLowerCase)
            .distinct()
            .sorted()
            .forEach(targets::add);

        if (targets.isEmpty()) {
            fallbackChannel
                .map(String::trim)
                .map(String::toLowerCase)
                .ifPresent(targets::add);
        }
        return targets;
    }

    public static Optional<Integer> parseSlaMinutes(Optional<String> rawMinutes) {
        if (rawMinutes == null) {
            throw new IllegalArgumentException("rawMinutes must not be null");
        }
        return rawMinutes
            .map(String::trim)
            .filter(value -> !value.isBlank())
            .flatMap(IncidentOpsOptionals::parsePositiveInt);
    }

    private static Optional<Integer> parsePositiveInt(String raw) {
        try {
            int value = Integer.parseInt(raw);
            return value > 0 ? Optional.of(value) : Optional.empty();
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    private static void validateNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must be non-blank");
        }
    }
}
