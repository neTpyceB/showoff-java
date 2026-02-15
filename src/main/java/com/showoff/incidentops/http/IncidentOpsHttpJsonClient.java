package com.showoff.incidentops.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public final class IncidentOpsHttpJsonClient {
    private static final String JSON_CONTENT_TYPE = "application/json";

    private IncidentOpsHttpJsonClient() {}

    public record IncidentPayload(String incidentId, String serviceId, int severity, String status) {
        public IncidentPayload {
            validateNonBlank(incidentId, "incidentId");
            validateNonBlank(serviceId, "serviceId");
            validateNonBlank(status, "status");
            if (severity < 1 || severity > 5) {
                throw new IllegalArgumentException("severity must be 1..5");
            }
        }
    }

    public static HttpRequest createGetRequest(URI uri, Duration timeout) {
        validateUriAndTimeout(uri, timeout);
        return HttpRequest.newBuilder(uri)
            .timeout(timeout)
            .header("Accept", JSON_CONTENT_TYPE)
            .GET()
            .build();
    }

    public static HttpRequest createPostRequest(URI uri, String jsonBody, Duration timeout) {
        validateUriAndTimeout(uri, timeout);
        validateNonBlank(jsonBody, "jsonBody");
        return HttpRequest.newBuilder(uri)
            .timeout(timeout)
            .header("Accept", JSON_CONTENT_TYPE)
            .header("Content-Type", JSON_CONTENT_TYPE)
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();
    }

    public static String sendSync(HttpClient client, HttpRequest request) {
        validateClientAndRequest(client, request);
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return ensureSuccessStatus(response);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("http request interrupted", ex);
        } catch (IOException ex) {
            throw new IllegalStateException("http request failed", ex);
        }
    }

    public static CompletableFuture<String> sendAsync(HttpClient client, HttpRequest request) {
        validateClientAndRequest(client, request);
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(IncidentOpsHttpJsonClient::ensureSuccessStatus);
    }

    public static String toJson(IncidentPayload payload, ObjectMapper mapper) {
        if (payload == null) {
            throw new IllegalArgumentException("payload must not be null");
        }
        if (mapper == null) {
            throw new IllegalArgumentException("mapper must not be null");
        }
        try {
            return mapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("failed to serialize payload", ex);
        }
    }

    public static IncidentPayload fromJson(String json, ObjectMapper mapper) {
        validateNonBlank(json, "json");
        if (mapper == null) {
            throw new IllegalArgumentException("mapper must not be null");
        }
        try {
            return mapper.readValue(json, IncidentPayload.class);
        } catch (IOException ex) {
            throw new IllegalArgumentException("failed to deserialize payload", ex);
        }
    }

    public static IncidentPayload fetchIncidentSync(
        HttpClient client,
        URI uri,
        ObjectMapper mapper,
        Duration timeout
    ) {
        if (mapper == null) {
            throw new IllegalArgumentException("mapper must not be null");
        }
        String json = sendSync(client, createGetRequest(uri, timeout));
        return fromJson(json, mapper);
    }

    public static CompletableFuture<IncidentPayload> fetchIncidentAsync(
        HttpClient client,
        URI uri,
        ObjectMapper mapper,
        Duration timeout
    ) {
        if (mapper == null) {
            throw new IllegalArgumentException("mapper must not be null");
        }
        return sendAsync(client, createGetRequest(uri, timeout))
            .thenApply(json -> fromJson(json, mapper));
    }

    public static IncidentPayload postIncidentSync(
        HttpClient client,
        URI uri,
        IncidentPayload payload,
        ObjectMapper mapper,
        Duration timeout
    ) {
        if (payload == null) {
            throw new IllegalArgumentException("payload must not be null");
        }
        if (mapper == null) {
            throw new IllegalArgumentException("mapper must not be null");
        }
        String json = toJson(payload, mapper);
        String responseJson = sendSync(client, createPostRequest(uri, json, timeout));
        return fromJson(responseJson, mapper);
    }

    private static String ensureSuccessStatus(HttpResponse<String> response) {
        int status = response.statusCode();
        if (status >= 200 && status < 300) {
            return response.body();
        }
        throw new IllegalStateException("unexpected http status: " + status);
    }

    private static void validateClientAndRequest(HttpClient client, HttpRequest request) {
        if (client == null) {
            throw new IllegalArgumentException("client must not be null");
        }
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
    }

    private static void validateUriAndTimeout(URI uri, Duration timeout) {
        if (uri == null) {
            throw new IllegalArgumentException("uri must not be null");
        }
        if (timeout == null) {
            throw new IllegalArgumentException("timeout must not be null");
        }
        if (timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("timeout must be > 0");
        }
    }

    private static void validateNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must be non-blank");
        }
    }
}
