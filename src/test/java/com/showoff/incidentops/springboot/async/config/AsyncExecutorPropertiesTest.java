package com.showoff.incidentops.springboot.async.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AsyncExecutorPropertiesTest {
    @Test
    void constructor_bindsValidValues() {
        AsyncExecutorProperties properties = new AsyncExecutorProperties(2, 8, 100, "incidentops-async-");

        assertEquals(2, properties.corePoolSize());
        assertEquals(8, properties.maxPoolSize());
        assertEquals(100, properties.queueCapacity());
        assertEquals("incidentops-async-", properties.threadNamePrefix());
    }

    @Test
    void constructor_rejectsInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> new AsyncExecutorProperties(0, 2, 10, "incidentops-async-"));
        assertThrows(IllegalArgumentException.class, () -> new AsyncExecutorProperties(3, 2, 10, "incidentops-async-"));
        assertThrows(IllegalArgumentException.class, () -> new AsyncExecutorProperties(1, 2, -1, "incidentops-async-"));
        assertThrows(IllegalArgumentException.class, () -> new AsyncExecutorProperties(1, 2, 10, null));
        assertThrows(IllegalArgumentException.class, () -> new AsyncExecutorProperties(1, 2, 10, " "));
    }
}
