package com.showoff.incidentops.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class IncidentOpsConcurrency {
    private IncidentOpsConcurrency() {}

    public static int dispatchNotifications(
        List<String> incidentIds,
        ExecutorService executor,
        Consumer<String> notifier
    ) {
        if (incidentIds == null) {
            throw new IllegalArgumentException("incidentIds must not be null");
        }
        if (executor == null) {
            throw new IllegalArgumentException("executor must not be null");
        }
        if (notifier == null) {
            throw new IllegalArgumentException("notifier must not be null");
        }

        List<Future<?>> futures = new ArrayList<>(incidentIds.size());
        for (String incidentId : incidentIds) {
            validateNonBlank(incidentId, "incidentId");
            String normalized = incidentId.trim().toUpperCase();
            futures.add(executor.submit((Runnable) () -> notifier.accept(normalized)));
        }

        waitForAll(futures);
        return futures.size();
    }

    public static List<Integer> runComputations(List<Callable<Integer>> tasks, ExecutorService executor) {
        if (tasks == null) {
            throw new IllegalArgumentException("tasks must not be null");
        }
        if (executor == null) {
            throw new IllegalArgumentException("executor must not be null");
        }

        List<Future<Integer>> futures = new ArrayList<>(tasks.size());
        for (Callable<Integer> task : tasks) {
            if (task == null) {
                throw new IllegalArgumentException("tasks must not contain null");
            }
            futures.add(executor.submit(task));
        }

        List<Integer> results = new ArrayList<>(futures.size());
        for (Future<Integer> future : futures) {
            try {
                results.add(future.get());
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("interrupted while waiting for task result", ex);
            } catch (ExecutionException ex) {
                throw new IllegalStateException("task failed", ex.getCause());
            }
        }
        return results;
    }

    public static CompletableFuture<String> fetchRunbookStepAsync(String serviceId, Executor executor) {
        validateNonBlank(serviceId, "serviceId");
        if (executor == null) {
            throw new IllegalArgumentException("executor must not be null");
        }
        return CompletableFuture.supplyAsync(() -> "open-runbook:" + serviceId.trim().toLowerCase(), executor);
    }

    public static CompletableFuture<String> buildEscalationMessageAsync(String incidentId, Executor executor) {
        validateNonBlank(incidentId, "incidentId");
        if (executor == null) {
            throw new IllegalArgumentException("executor must not be null");
        }
        return CompletableFuture
            .supplyAsync(() -> incidentId.trim().toUpperCase(), executor)
            .thenApply(id -> "ESCALATE " + id)
            .thenApply(message -> message + " via on-call");
    }

    public static CompletableFuture<Integer> combinedErrorBudgetBurnAsync(
        String primaryServiceId,
        String secondaryServiceId,
        Executor executor
    ) {
        validateNonBlank(primaryServiceId, "primaryServiceId");
        validateNonBlank(secondaryServiceId, "secondaryServiceId");
        if (executor == null) {
            throw new IllegalArgumentException("executor must not be null");
        }

        CompletableFuture<Integer> primary = CompletableFuture.supplyAsync(
            () -> primaryServiceId.trim().length(),
            executor
        );
        CompletableFuture<Integer> secondary = CompletableFuture.supplyAsync(
            () -> secondaryServiceId.trim().length(),
            executor
        );
        return primary.thenCombine(secondary, Integer::sum);
    }

    public static boolean shutdownAndAwait(ExecutorService executor, long timeoutMillis) {
        if (executor == null) {
            throw new IllegalArgumentException("executor must not be null");
        }
        if (timeoutMillis < 0) {
            throw new IllegalArgumentException("timeoutMillis must be >= 0");
        }
        executor.shutdown();
        try {
            if (executor.awaitTermination(timeoutMillis, TimeUnit.MILLISECONDS)) {
                return true;
            }
            executor.shutdownNow();
            return false;
        } catch (InterruptedException ex) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private static void waitForAll(List<Future<?>> futures) {
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("interrupted while waiting for notification tasks", ex);
            } catch (ExecutionException ex) {
                throw new IllegalStateException("notification task failed", ex.getCause());
            }
        }
    }

    private static void validateNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must be non-blank");
        }
    }
}
