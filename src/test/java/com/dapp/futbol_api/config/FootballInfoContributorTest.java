package com.dapp.futbol_api.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.info.Info;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FootballInfoContributorTest {

    private FootballInfoContributor contributor;
    private Info.Builder builder;

    @BeforeEach
    void setUp() {
        contributor = new FootballInfoContributor();
        builder = new Info.Builder();
    }

    @Test
    void contribute() {
        contributor.contribute(builder);
        Info info = builder.build();

        Map<String, Object> details = info.getDetails();

        // Verify application info
        Map<String, Object> appInfo = (Map<String, Object>) details.get("application");
        assertNotNull(appInfo);
        assertEquals("Football API", appInfo.get("name"));
        assertEquals("3.0.0", appInfo.get("version"));
        assertEquals("Sistema de gestión deportiva - Entrega 3", appInfo.get("description"));
        assertEquals("Grupo M", appInfo.get("team"));
        assertEquals("Operational", appInfo.get("status"));
        assertNotNull(appInfo.get("timestamp"));

        // Verify technical features
        Map<String, Object> features = (Map<String, Object>) details.get("technicalFeatures");
        assertNotNull(features);
        assertEquals("JWT Security", features.get("authentication"));
        assertEquals("Scraper Service Integration", features.get("dataSource"));
        assertEquals("Spring Boot Actuator", features.get("monitoring"));
        assertEquals("H2 In-Memory", features.get("database"));
        assertEquals("Swagger/OpenAPI 3", features.get("documentation"));
        assertEquals("GitHub Actions + Render", features.get("ciCd"));

        // Verify available endpoints
        Map<String, Object> endpoints = (Map<String, Object>) details.get("availableEndpoints");
        assertNotNull(endpoints);
        assertEquals("/api/teams/{id}/players", endpoints.get("players"));
        assertEquals("/api/teams/{id}/upcoming-matches", endpoints.get("matches"));
        assertEquals("/api/players/{id}/performance", endpoints.get("performance"));
        assertEquals("/api/matches/prediction", endpoints.get("prediction"));

        // Verify monitoring info
        assertEquals("Visit /monitoring/health for status", details.get("monitoring"));
    }
}
