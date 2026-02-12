package com.showoff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class IncidentOpsReliabilityTest {
    @Test
    void parseRetryBudgetOrDefault_handlesValidAndFallbackCases() {
        assertEquals(3, IncidentOpsReliability.parseRetryBudgetOrDefault("3", 10));
        assertEquals(10, IncidentOpsReliability.parseRetryBudgetOrDefault("-1", 10));
        assertEquals(10, IncidentOpsReliability.parseRetryBudgetOrDefault("no-int", 10));
        assertEquals(10, IncidentOpsReliability.parseRetryBudgetOrDefault(" ", 10));
        assertEquals(10, IncidentOpsReliability.parseRetryBudgetOrDefault(null, 10));
    }

    @Test
    void parseTimeoutSeconds_parsesOrThrowsValidationException() {
        assertEquals(2, IncidentOpsReliability.parseTimeoutSeconds("2500"));

        IncidentValidationException invalidNumber = assertThrows(
            IncidentValidationException.class,
            () -> IncidentOpsReliability.parseTimeoutSeconds("not-a-number")
        );
        assertInstanceOf(NumberFormatException.class, invalidNumber.getCause());

        IncidentValidationException overflow = assertThrows(
            IncidentValidationException.class,
            () -> IncidentOpsReliability.parseTimeoutSeconds("99999999999999")
        );
        assertInstanceOf(ArithmeticException.class, overflow.getCause());

        assertThrows(IncidentValidationException.class, () -> IncidentOpsReliability.parseTimeoutSeconds("-10"));
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsReliability.parseTimeoutSeconds(" "));
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsReliability.parseTimeoutSeconds(null));
    }

    @Test
    void normalizeIncidentId_returnsNormalizedValueOrThrowsUnchecked() {
        assertEquals("INC-9001", IncidentOpsReliability.normalizeIncidentId(" inc-9001 "));

        IncidentValidationException ex = assertThrows(
            IncidentValidationException.class,
            () -> IncidentOpsReliability.normalizeIncidentId(" ")
        );
        assertInstanceOf(IllegalArgumentException.class, ex.getCause());
    }

    @Test
    void loadRunbookStep_returnsTrimmedStepOrThrowsChecked() throws RunbookAccessException {
        Map<String, String> runbook = new LinkedHashMap<>();
        runbook.put("payments-api", " restart workers ");
        runbook.put("identity-api", " ");

        assertEquals("restart workers", IncidentOpsReliability.loadRunbookStep(runbook, "payments-api"));

        assertThrows(RunbookAccessException.class, () -> IncidentOpsReliability.loadRunbookStep(runbook, "search-api"));
        assertThrows(RunbookAccessException.class, () -> IncidentOpsReliability.loadRunbookStep(runbook, "identity-api"));
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsReliability.loadRunbookStep(null, "payments-api"));
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsReliability.loadRunbookStep(runbook, " "));
    }

    @Test
    void parseRunbookActions_tryWithResourcesClosesOnSuccessAndSkipsNoise() throws RunbookAccessException {
        TestRunbookSource source = new TestRunbookSource(
            List.of("  # comment", " drain queue ", "", "restart pods", "   "),
            -1,
            false
        );

        List<String> actions = IncidentOpsReliability.parseRunbookActions(source);
        assertEquals(List.of("drain queue", "restart pods"), actions);
        assertTrue(source.isClosed());
    }

    @Test
    void parseRunbookActions_tryWithResourcesClosesOnFailureAndWrapsChecked() {
        TestRunbookSource source = new TestRunbookSource(
            List.of("drain queue", "restart pods"),
            1,
            false
        );
        RunbookAccessException ex = assertThrows(
            RunbookAccessException.class,
            () -> IncidentOpsReliability.parseRunbookActions(source)
        );
        assertInstanceOf(IOException.class, ex.getCause());
        assertTrue(source.isClosed());
    }

    @Test
    void parseRunbookActions_wrapsCloseFailureAndValidatesInput() {
        TestRunbookSource source = new TestRunbookSource(List.of("drain queue"), -1, true);
        RunbookAccessException ex = assertThrows(
            RunbookAccessException.class,
            () -> IncidentOpsReliability.parseRunbookActions(source)
        );
        assertInstanceOf(IOException.class, ex.getCause());
        assertTrue(source.isClosed());

        assertThrows(IllegalArgumentException.class, () -> IncidentOpsReliability.parseRunbookActions(null));
    }

    @Test
    void firstReachableChannel_usesOptionalStream() {
        List<Optional<String>> channels = new ArrayList<>();
        channels.add(Optional.empty());
        channels.add(Optional.of(" "));
        channels.add(Optional.of(" #payments-oncall "));
        channels.add(Optional.of("#platform-oncall"));

        assertEquals(Optional.of("#payments-oncall"), IncidentOpsReliability.firstReachableChannel(channels));
        assertEquals(Optional.empty(), IncidentOpsReliability.firstReachableChannel(List.of(Optional.empty(), Optional.of(" "))));
    }

    @Test
    void firstReachableChannel_validatesInput() {
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsReliability.firstReachableChannel(null));
        assertThrows(
            IllegalArgumentException.class,
            () -> {
                List<Optional<String>> channels = new ArrayList<>(List.of(Optional.of("#payments")));
                channels.add(null);
                IncidentOpsReliability.firstReachableChannel(channels);
            }
        );
        assertFalse(IncidentOpsReliability.firstReachableChannel(List.of(Optional.empty())).isPresent());
    }

    private static final class TestRunbookSource implements IncidentOpsReliability.RunbookSource {
        private final List<String> lines;
        private final int failAtReadCount;
        private final boolean failOnClose;
        private int reads;
        private boolean closed;

        private TestRunbookSource(List<String> lines, int failAtReadCount, boolean failOnClose) {
            this.lines = lines;
            this.failAtReadCount = failAtReadCount;
            this.failOnClose = failOnClose;
        }

        @Override
        public String readLine() throws IOException {
            if (failAtReadCount >= 0 && reads == failAtReadCount) {
                throw new IOException("simulated read failure");
            }
            if (reads >= lines.size()) {
                return null;
            }
            return lines.get(reads++);
        }

        @Override
        public void close() throws IOException {
            closed = true;
            if (failOnClose) {
                throw new IOException("simulated close failure");
            }
        }

        private boolean isClosed() {
            return closed;
        }
    }
}
