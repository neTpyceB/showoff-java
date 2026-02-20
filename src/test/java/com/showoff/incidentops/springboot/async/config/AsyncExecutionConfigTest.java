package com.showoff.incidentops.springboot.async.config;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AsyncExecutionConfigTest {
    @Test
    void incidentOpsAsyncExecutor_usesConfiguredThreadPoolSettings() throws Exception {
        AsyncExecutionConfig config = new AsyncExecutionConfig();
        AsyncExecutorProperties properties = new AsyncExecutorProperties(2, 6, 40, "incidentops-async-");

        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) config.incidentOpsAsyncExecutor(properties);
        try {
            assertEquals(2, executor.getCorePoolSize());
            assertEquals(6, executor.getMaxPoolSize());
            assertEquals(40, executor.getThreadPoolExecutor().getQueue().remainingCapacity());

            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<String> threadName = new AtomicReference<>();
            executor.execute(() -> {
                threadName.set(Thread.currentThread().getName());
                latch.countDown();
            });

            assertTrue(latch.await(2, TimeUnit.SECONDS));
            assertTrue(threadName.get().startsWith("incidentops-async-"));
        } finally {
            executor.shutdown();
        }
    }
}
