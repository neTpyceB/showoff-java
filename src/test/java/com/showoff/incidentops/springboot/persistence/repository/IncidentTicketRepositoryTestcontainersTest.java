package com.showoff.incidentops.springboot.persistence.repository;

import com.showoff.incidentops.springboot.persistence.entity.IncidentTicketEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(
    classes = com.showoff.incidentops.springboot.IncidentApiApplication.class,
    properties = {
        "spring.main.web-application-type=none",
        "spring.main.banner-mode=off",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true"
    }
)
class IncidentTicketRepositoryTestcontainersTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17")
        .withDatabaseName("incidentops_test")
        .withUsername("incidentops")
        .withPassword("incidentops");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
        registry.add("spring.flyway.url", POSTGRES::getJdbcUrl);
        registry.add("spring.flyway.user", POSTGRES::getUsername);
        registry.add("spring.flyway.password", POSTGRES::getPassword);
    }

    @Autowired
    private IncidentTicketRepository repository;

    @Test
    void repository_worksAgainstRealPostgresContainer() {
        IncidentTicketEntity saved = repository.save(
            new IncidentTicketEntity("TKT-8801", "payments-api", 5, "db lock contention", "OPEN")
        );
        assertTrue(saved.getId() != null);

        IncidentTicketEntity fetched = repository.findByTicketId("TKT-8801").orElseThrow();
        assertEquals("payments-api", fetched.getServiceId());
        assertEquals(5, fetched.getSeverity());
        assertEquals("OPEN", fetched.getStatus());
    }
}
