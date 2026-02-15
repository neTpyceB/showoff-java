package com.showoff.incidentops.springboot;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;

class IncidentApiApplicationTest {
    @AfterEach
    void cleanup() {
        IncidentApiApplication.shutdown();
    }

    @Test
    void launch_startsContext() {
        ConfigurableApplicationContext context = IncidentApiApplication.launch(
            new String[] {"--spring.main.web-application-type=none", "--spring.main.banner-mode=off"}
        );
        try {
            assertNotNull(context.getBean(IncidentController.class));
            assertNotNull(context.getBean(IncidentService.class));
        } finally {
            context.close();
        }
    }

    @Test
    void main_andShutdown_coverApplicationEntryLifecycle() {
        IncidentApiApplication.main(new String[] {"--spring.main.web-application-type=none", "--spring.main.banner-mode=off"});
        IncidentApiApplication.shutdown();
        IncidentApiApplication.shutdown();
    }
}
