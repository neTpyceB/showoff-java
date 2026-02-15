package com.showoff.incidentops.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.junit.jupiter.api.Test;

class IncidentOpsHttpJsonClientTest {
    private static final Duration TIMEOUT = Duration.ofSeconds(2);

    @Test
    void createRequests_configuresImmutableHttpRequests() {
        URI uri = URI.create("http://localhost/api/incidents/INC-1");

        HttpRequest get = IncidentOpsHttpJsonClient.createGetRequest(uri, TIMEOUT);
        assertEquals("GET", get.method());
        assertEquals(uri, get.uri());
        assertEquals(Optional.of(TIMEOUT), get.timeout());
        assertEquals(List.of("application/json"), get.headers().allValues("Accept"));

        HttpRequest post = IncidentOpsHttpJsonClient.createPostRequest(uri, "{\"ok\":true}", TIMEOUT);
        assertEquals("POST", post.method());
        assertEquals(List.of("application/json"), post.headers().allValues("Content-Type"));
        assertTrue(post.bodyPublisher().isPresent());
    }

    @Test
    void jsonRoundTrip_usesRecordAndJackson() {
        ObjectMapper mapper = new ObjectMapper();
        IncidentOpsHttpJsonClient.IncidentPayload payload =
            new IncidentOpsHttpJsonClient.IncidentPayload("INC-1", "payments-api", 4, "OPEN");

        String json = IncidentOpsHttpJsonClient.toJson(payload, mapper);
        IncidentOpsHttpJsonClient.IncidentPayload restored = IncidentOpsHttpJsonClient.fromJson(json, mapper);
        assertEquals(payload, restored);
    }

    @Test
    void httpSyncAsyncAndPost_coverHttpJsonFlows() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AtomicReference<String> postedBody = new AtomicReference<>();
        HttpServer server = startServer(postedBody);
        try {
            HttpClient client = HttpClient.newHttpClient();
            URI base = URI.create("http://localhost:" + server.getAddress().getPort());

            IncidentOpsHttpJsonClient.IncidentPayload syncPayload = IncidentOpsHttpJsonClient.fetchIncidentSync(
                client,
                base.resolve("/incident"),
                mapper,
                TIMEOUT
            );
            assertEquals("INC-1001", syncPayload.incidentId());

            IncidentOpsHttpJsonClient.IncidentPayload asyncPayload = IncidentOpsHttpJsonClient.fetchIncidentAsync(
                client,
                base.resolve("/incident"),
                mapper,
                TIMEOUT
            ).join();
            assertEquals("payments-api", asyncPayload.serviceId());

            IncidentOpsHttpJsonClient.IncidentPayload posted = IncidentOpsHttpJsonClient.postIncidentSync(
                client,
                base.resolve("/incident"),
                new IncidentOpsHttpJsonClient.IncidentPayload("INC-7777", "search-api", 3, "OPEN"),
                mapper,
                TIMEOUT
            );
            assertEquals("INC-7777", posted.incidentId());
            assertTrue(postedBody.get().contains("\"INC-7777\""));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void errorStatusesAndParsingFailures_areReported() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        HttpServer server = startServer(new AtomicReference<>());
        try {
            HttpClient client = HttpClient.newHttpClient();
            URI base = URI.create("http://localhost:" + server.getAddress().getPort());
            HttpRequest errorRequest = IncidentOpsHttpJsonClient.createGetRequest(base.resolve("/error"), TIMEOUT);

            assertThrows(IllegalStateException.class, () -> IncidentOpsHttpJsonClient.sendSync(client, errorRequest));
            assertThrows(
                CompletionException.class,
                () -> IncidentOpsHttpJsonClient.sendAsync(client, errorRequest).join()
            );
            assertThrows(
                IllegalArgumentException.class,
                () -> IncidentOpsHttpJsonClient.fetchIncidentSync(client, base.resolve("/bad-json"), mapper, TIMEOUT)
            );
        } finally {
            server.stop(0);
        }
    }

    @Test
    void validations_andClientFailurePaths_areCovered() {
        ObjectMapper mapper = new ObjectMapper();
        URI uri = URI.create("http://localhost/test");
        HttpClient failingIoClient = new ThrowingHttpClient(new IOException("io failure"), null);
        HttpClient failingInterruptedClient = new ThrowingHttpClient(null, new InterruptedException("interrupted"));

        assertThrows(IllegalArgumentException.class, () -> IncidentOpsHttpJsonClient.createGetRequest(null, TIMEOUT));
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsHttpJsonClient.createGetRequest(uri, null));
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsHttpJsonClient.createGetRequest(uri, Duration.ZERO)
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsHttpJsonClient.createPostRequest(uri, " ", TIMEOUT)
        );
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsHttpJsonClient.sendSync(null, null));
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsHttpJsonClient.sendAsync(null, null));
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsHttpJsonClient.toJson(null, mapper));
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsHttpJsonClient.toJson(
            new IncidentOpsHttpJsonClient.IncidentPayload("INC-1", "payments-api", 3, "OPEN"),
            null
        ));
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsHttpJsonClient.fromJson(" ", mapper));
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsHttpJsonClient.fromJson("{}", null));
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsHttpJsonClient.fetchIncidentSync(HttpClient.newHttpClient(), uri, null, TIMEOUT)
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsHttpJsonClient.fetchIncidentAsync(HttpClient.newHttpClient(), uri, null, TIMEOUT)
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsHttpJsonClient.postIncidentSync(HttpClient.newHttpClient(), uri, null, mapper, TIMEOUT)
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsHttpJsonClient.postIncidentSync(
                HttpClient.newHttpClient(),
                uri,
                new IncidentOpsHttpJsonClient.IncidentPayload("INC-1", "payments-api", 3, "OPEN"),
                null,
                TIMEOUT
            )
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> new IncidentOpsHttpJsonClient.IncidentPayload(" ", "payments-api", 3, "OPEN")
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> new IncidentOpsHttpJsonClient.IncidentPayload("INC-1", " ", 3, "OPEN")
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> new IncidentOpsHttpJsonClient.IncidentPayload("INC-1", "payments-api", 6, "OPEN")
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> new IncidentOpsHttpJsonClient.IncidentPayload("INC-1", "payments-api", 3, " ")
        );

        HttpRequest request = IncidentOpsHttpJsonClient.createGetRequest(uri, TIMEOUT);
        IllegalStateException ioEx = assertThrows(
            IllegalStateException.class,
            () -> IncidentOpsHttpJsonClient.sendSync(failingIoClient, request)
        );
        assertInstanceOf(IOException.class, ioEx.getCause());

        Thread.currentThread().interrupt();
        try {
            IllegalStateException interruptedEx = assertThrows(
                IllegalStateException.class,
                () -> IncidentOpsHttpJsonClient.sendSync(failingInterruptedClient, request)
            );
            assertInstanceOf(InterruptedException.class, interruptedEx.getCause());
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }

    private static HttpServer startServer(AtomicReference<String> postedBody) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.setExecutor(Executors.newCachedThreadPool());
        server.createContext("/incident", new JsonHandler(postedBody));
        server.createContext("/error", exchange -> writeResponse(exchange, 500, "{\"error\":\"boom\"}"));
        server.createContext("/bad-json", exchange -> writeResponse(exchange, 200, "{invalid"));
        server.start();
        return server;
    }

    private static void writeResponse(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes();
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private static final class JsonHandler implements HttpHandler {
        private final AtomicReference<String> postedBody;

        private JsonHandler(AtomicReference<String> postedBody) {
            this.postedBody = postedBody;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            if ("GET".equals(method)) {
                writeResponse(exchange, 200, "{\"incidentId\":\"INC-1001\",\"serviceId\":\"payments-api\",\"severity\":4,\"status\":\"OPEN\"}");
                return;
            }
            if ("POST".equals(method)) {
                String requestBody = new String(exchange.getRequestBody().readAllBytes());
                postedBody.set(requestBody);
                writeResponse(exchange, 200, requestBody);
                return;
            }
            writeResponse(exchange, 405, "{\"error\":\"method not allowed\"}");
        }
    }

    private static final class ThrowingHttpClient extends HttpClient {
        private final IOException ioException;
        private final InterruptedException interruptedException;

        private ThrowingHttpClient(IOException ioException, InterruptedException interruptedException) {
            this.ioException = ioException;
            this.interruptedException = interruptedException;
        }

        @Override
        public Optional<java.net.CookieHandler> cookieHandler() {
            return Optional.empty();
        }

        @Override
        public Optional<Duration> connectTimeout() {
            return Optional.of(Duration.ofSeconds(1));
        }

        @Override
        public Redirect followRedirects() {
            return Redirect.NEVER;
        }

        @Override
        public Optional<ProxySelector> proxy() {
            return Optional.empty();
        }

        @Override
        public SSLContext sslContext() {
            try {
                SSLContext context = SSLContext.getInstance("TLS");
                context.init(null, new TrustManager[] {new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {}

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {}

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[0];
                    }
                }}, new SecureRandom());
                return context;
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        public SSLParameters sslParameters() {
            return new SSLParameters();
        }

        @Override
        public Optional<java.net.Authenticator> authenticator() {
            return Optional.empty();
        }

        @Override
        public Version version() {
            return Version.HTTP_1_1;
        }

        @Override
        public Optional<Executor> executor() {
            return Optional.empty();
        }

        @Override
        public <T> HttpResponse<T> send(HttpRequest req, HttpResponse.BodyHandler<T> responseBodyHandler)
            throws IOException, InterruptedException {
            if (interruptedException != null) {
                throw interruptedException;
            }
            throw ioException;
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(
            HttpRequest req,
            HttpResponse.BodyHandler<T> responseBodyHandler
        ) {
            return CompletableFuture.failedFuture(new IOException("async not supported in throwing stub"));
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(
            HttpRequest req,
            HttpResponse.BodyHandler<T> responseBodyHandler,
            HttpResponse.PushPromiseHandler<T> pushPromiseHandler
        ) {
            return CompletableFuture.failedFuture(new IOException("async push not supported in throwing stub"));
        }
    }
}
