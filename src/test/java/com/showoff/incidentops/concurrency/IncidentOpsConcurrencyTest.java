package com.showoff.incidentops.concurrency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

class IncidentOpsConcurrencyTest {
    @Test
    void dispatchNotifications_usesRunnableAndExecutorService() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            List<String> delivered = new CopyOnWriteArrayList<>();
            int count = IncidentOpsConcurrency.dispatchNotifications(
                List.of("inc-1001", " INC-1002 "),
                executor,
                delivered::add
            );
            assertEquals(2, count);
            assertEquals(Set.of("INC-1001", "INC-1002"), Set.copyOf(delivered));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void dispatchNotifications_validatesAndWrapsTaskFailures() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            assertThrows(
                IllegalArgumentException.class,
                () -> IncidentOpsConcurrency.dispatchNotifications(null, executor, id -> {})
            );
            assertThrows(
                IllegalArgumentException.class,
                () -> IncidentOpsConcurrency.dispatchNotifications(List.of("INC-1"), null, id -> {})
            );
            assertThrows(
                IllegalArgumentException.class,
                () -> IncidentOpsConcurrency.dispatchNotifications(List.of("INC-1"), executor, null)
            );
            assertThrows(
                IllegalArgumentException.class,
                () -> IncidentOpsConcurrency.dispatchNotifications(new ArrayList<>(List.of(" ")), executor, id -> {})
            );
            assertThrows(
                IllegalArgumentException.class,
                () -> {
                    List<String> ids = new ArrayList<>(List.of("INC-1"));
                    ids.add(null);
                    IncidentOpsConcurrency.dispatchNotifications(ids, executor, id -> {});
                }
            );
            assertThrows(
                IllegalStateException.class,
                () -> IncidentOpsConcurrency.dispatchNotifications(List.of("INC-1"), executor, id -> {
                    throw new IllegalArgumentException("simulated notifier failure");
                })
            );
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void runComputations_usesCallableAndReturnsResults() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            List<Callable<Integer>> tasks = List.of(
                () -> 2 + 3,
                () -> 10 / 2
            );
            assertIterableEquals(List.of(5, 5), IncidentOpsConcurrency.runComputations(tasks, executor));
            assertIterableEquals(List.of(), IncidentOpsConcurrency.runComputations(List.of(), executor));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void runComputations_validatesAndWrapsTaskFailures() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            assertThrows(
                IllegalArgumentException.class,
                () -> IncidentOpsConcurrency.runComputations(null, executor)
            );
            assertThrows(
                IllegalArgumentException.class,
                () -> IncidentOpsConcurrency.runComputations(List.of(() -> 1), null)
            );
            assertThrows(
                IllegalArgumentException.class,
                () -> {
                    List<Callable<Integer>> tasks = new ArrayList<>(List.of(() -> 1));
                    tasks.add(null);
                    IncidentOpsConcurrency.runComputations(tasks, executor);
                }
            );
            assertThrows(
                IllegalStateException.class,
                () -> IncidentOpsConcurrency.runComputations(List.of(() -> {
                    throw new IllegalArgumentException("simulated task failure");
                }), executor)
            );
            Thread.currentThread().interrupt();
            try {
                IllegalStateException interrupted = assertThrows(
                    IllegalStateException.class,
                    () -> IncidentOpsConcurrency.runComputations(List.of(() -> {
                        TimeUnit.SECONDS.sleep(1);
                        return 1;
                    }), executor)
                );
                assertTrue(interrupted.getCause() instanceof InterruptedException);
            } finally {
                Thread.interrupted();
            }
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void completableFuture_basicChainingAndCombine() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            CompletableFuture<String> runbook = IncidentOpsConcurrency.fetchRunbookStepAsync(" Payments-Api ", executor);
            CompletableFuture<String> message = IncidentOpsConcurrency.buildEscalationMessageAsync(" inc-9001 ", executor);
            CompletableFuture<Integer> combined = IncidentOpsConcurrency.combinedErrorBudgetBurnAsync(
                "payments-api",
                "identity-api",
                executor
            );

            assertEquals("open-runbook:payments-api", runbook.join());
            assertEquals("ESCALATE INC-9001 via on-call", message.join());
            assertEquals("payments-api".length() + "identity-api".length(), combined.join());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void completableFuture_methods_validateInput() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            assertThrows(
                IllegalArgumentException.class,
                () -> IncidentOpsConcurrency.fetchRunbookStepAsync(" ", executor)
            );
            assertThrows(
                IllegalArgumentException.class,
                () -> IncidentOpsConcurrency.fetchRunbookStepAsync("payments-api", null)
            );
            assertThrows(
                IllegalArgumentException.class,
                () -> IncidentOpsConcurrency.buildEscalationMessageAsync(" ", executor)
            );
            assertThrows(
                IllegalArgumentException.class,
                () -> IncidentOpsConcurrency.buildEscalationMessageAsync("INC-1", null)
            );
            assertThrows(
                IllegalArgumentException.class,
                () -> IncidentOpsConcurrency.combinedErrorBudgetBurnAsync(" ", "identity-api", executor)
            );
            assertThrows(
                IllegalArgumentException.class,
                () -> IncidentOpsConcurrency.combinedErrorBudgetBurnAsync("payments-api", " ", executor)
            );
            assertThrows(
                IllegalArgumentException.class,
                () -> IncidentOpsConcurrency.combinedErrorBudgetBurnAsync("payments-api", "identity-api", null)
            );
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void shutdownAndAwait_handlesTerminationTimeoutAndInterruption() throws Exception {
        ExecutorService fastExecutor = Executors.newSingleThreadExecutor();
        assertTrue(IncidentOpsConcurrency.shutdownAndAwait(fastExecutor, 200));

        ExecutorService slowExecutor = Executors.newSingleThreadExecutor();
        slowExecutor.submit(() -> {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        });
        assertFalse(IncidentOpsConcurrency.shutdownAndAwait(slowExecutor, 0));

        ExecutorService interruptedExecutor = Executors.newSingleThreadExecutor();
        interruptedExecutor.submit(() -> {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        });
        Thread.currentThread().interrupt();
        try {
            assertFalse(IncidentOpsConcurrency.shutdownAndAwait(interruptedExecutor, 100));
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
            interruptedExecutor.shutdownNow();
        }

        ExecutorService interruptedWaitExecutor = Executors.newSingleThreadExecutor();
        CountDownLatch notifierGate = new CountDownLatch(1);
        Thread.currentThread().interrupt();
        try {
            IllegalStateException interrupted = assertThrows(
                IllegalStateException.class,
                () -> IncidentOpsConcurrency.dispatchNotifications(
                    List.of("INC-7001"),
                    interruptedWaitExecutor,
                    id -> {
                        try {
                            notifierGate.await();
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                    }
                )
            );
            assertTrue(interrupted.getCause() instanceof InterruptedException);
        } finally {
            Thread.interrupted();
            notifierGate.countDown();
            interruptedWaitExecutor.shutdownNow();
        }

        assertThrows(
            IllegalArgumentException.class,
            () -> IncidentOpsConcurrency.shutdownAndAwait(null, 10)
        );
        ExecutorService invalidTimeoutExecutor = Executors.newSingleThreadExecutor();
        try {
            assertThrows(
                IllegalArgumentException.class,
                () -> IncidentOpsConcurrency.shutdownAndAwait(invalidTimeoutExecutor, -1)
            );
        } finally {
            invalidTimeoutExecutor.shutdownNow();
        }
    }
}
