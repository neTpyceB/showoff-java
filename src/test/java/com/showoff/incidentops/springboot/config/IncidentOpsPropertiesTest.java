package com.showoff.incidentops.springboot.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(
    classes = com.showoff.incidentops.springboot.IncidentApiApplication.class,
    properties = {"spring.main.web-application-type=none", "spring.main.banner-mode=off"}
)
class IncidentOpsPropertiesTest {
    @Autowired
    private IncidentOpsProperties properties;

    @Test
    void properties_areBoundUsingTypeSafeConfig() {
        assertEquals("OPEN", properties.tickets().defaultStatus());
        assertEquals(100, properties.tickets().maxPageSize());
        assertEquals("localhost", properties.integrations().redis().host());
        assertEquals(6379, properties.integrations().redis().port());
        assertEquals("localhost", properties.integrations().rabbitmq().host());
        assertEquals(5672, properties.integrations().rabbitmq().port());
        assertEquals("test-api-key", properties.security().apiKey());
        assertEquals("test-signing-secret", properties.security().signingSecret());
    }
}
