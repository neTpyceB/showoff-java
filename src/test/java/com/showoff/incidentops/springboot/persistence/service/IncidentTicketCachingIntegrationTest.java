package com.showoff.incidentops.springboot.persistence.service;

import com.showoff.incidentops.springboot.persistence.entity.IncidentTicketEntity;
import com.showoff.incidentops.springboot.persistence.repository.IncidentTicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(
    classes = com.showoff.incidentops.springboot.IncidentApiApplication.class,
    properties = {
        "spring.main.web-application-type=none",
        "spring.main.banner-mode=off",
        "spring.cache.type=simple"
    }
)
class IncidentTicketCachingIntegrationTest {
    @Autowired
    private IncidentTicketService service;

    @Autowired
    private IncidentTicketRepository repository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        clearCache(IncidentTicketService.CACHE_TICKET_BY_ID);
        clearCache(IncidentTicketService.CACHE_STATUS_PAGES);
        clearCache(IncidentTicketService.CACHE_SERVICE_SEARCH_PAGES);
    }

    @Test
    void repeatedGetByTicketId_readsFromCacheAfterFirstLookup() {
        repository.save(new IncidentTicketEntity("TKT-9701", "payments-api", 4, "queue delay", "OPEN"));

        var first = service.getByTicketId("tkt-9701");
        repository.deleteAll();
        var second = service.getByTicketId("TKT-9701");

        assertEquals(first, second);
        Cache cache = cacheManager.getCache(IncidentTicketService.CACHE_TICKET_BY_ID);
        assertNotNull(cache);
        assertNotNull(cache.get("TKT-9701"));
    }

    @Test
    void updateStatus_refreshesCachedTicket() {
        repository.save(new IncidentTicketEntity("TKT-9702", "identity-api", 3, "token issue", "OPEN"));

        service.getByTicketId("TKT-9702");
        var updated = service.updateStatus("TKT-9702", "resolved");
        repository.deleteAll();
        var afterUpdate = service.getByTicketId("TKT-9702");

        assertEquals("RESOLVED", updated.status());
        assertEquals("RESOLVED", afterUpdate.status());
    }

    private void clearCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }
}
