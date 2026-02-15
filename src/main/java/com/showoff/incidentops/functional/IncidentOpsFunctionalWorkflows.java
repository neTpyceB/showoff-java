package com.showoff.incidentops.functional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class IncidentOpsFunctionalWorkflows {
    private IncidentOpsFunctionalWorkflows() {}

    @FunctionalInterface
    public interface RoutingStrategy {
        String route(String serviceId, int errorRate);

        default String routeNormalized(String serviceId, int errorRate) {
            return route(serviceId.trim().toLowerCase(), errorRate);
        }
    }

    public static List<String> transformServiceIds(List<String> serviceIds, Function<String, String> transformer) {
        if (serviceIds == null) {
            throw new IllegalArgumentException("serviceIds must not be null");
        }
        if (transformer == null) {
            throw new IllegalArgumentException("transformer must not be null");
        }
        List<String> transformed = new ArrayList<>(serviceIds.size());
        for (String serviceId : serviceIds) {
            validateNonBlank(serviceId, "serviceId");
            String value = transformer.apply(serviceId);
            validateNonBlank(value, "transformedServiceId");
            transformed.add(value);
        }
        return transformed;
    }

    public static List<String> selectEscalationCandidates(List<String> incidentIds, Predicate<String> rule) {
        if (incidentIds == null) {
            throw new IllegalArgumentException("incidentIds must not be null");
        }
        if (rule == null) {
            throw new IllegalArgumentException("rule must not be null");
        }
        List<String> selected = new ArrayList<>();
        for (String incidentId : incidentIds) {
            validateNonBlank(incidentId, "incidentId");
            if (rule.test(incidentId)) {
                selected.add(incidentId);
            }
        }
        return selected;
    }

    public static int sendPagerNotifications(List<String> channels, Consumer<String> notifier) {
        if (channels == null) {
            throw new IllegalArgumentException("channels must not be null");
        }
        if (notifier == null) {
            throw new IllegalArgumentException("notifier must not be null");
        }
        int sent = 0;
        for (String channel : channels) {
            validateNonBlank(channel, "channel");
            notifier.accept(channel);
            sent++;
        }
        return sent;
    }

    public static String resolveFallbackChannel(Supplier<String> channelSupplier) {
        if (channelSupplier == null) {
            throw new IllegalArgumentException("channelSupplier must not be null");
        }
        String channel = channelSupplier.get();
        validateNonBlank(channel, "channel");
        return channel.trim().toLowerCase();
    }

    public static Map<String, String> buildRoutingTable(
        Map<String, Integer> serviceErrorRates,
        RoutingStrategy strategy
    ) {
        if (serviceErrorRates == null) {
            throw new IllegalArgumentException("serviceErrorRates must not be null");
        }
        if (strategy == null) {
            throw new IllegalArgumentException("strategy must not be null");
        }
        Map<String, String> table = new LinkedHashMap<>(serviceErrorRates.size());
        for (Map.Entry<String, Integer> entry : serviceErrorRates.entrySet()) {
            String serviceId = entry.getKey();
            Integer errorRate = entry.getValue();
            validateNonBlank(serviceId, "serviceId");
            if (errorRate == null || errorRate < 0) {
                throw new IllegalArgumentException("errorRate must be >= 0");
            }
            String channel = strategy.route(serviceId, errorRate);
            validateNonBlank(channel, "channel");
            table.put(serviceId, channel);
        }
        return table;
    }

    private static void validateNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must be non-blank");
        }
    }
}
