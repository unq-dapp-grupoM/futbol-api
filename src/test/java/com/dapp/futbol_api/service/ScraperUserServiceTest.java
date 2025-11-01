package com.dapp.futbol_api.service;

import com.dapp.futbol_api.security.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestClientException;

import static org.junit.jupiter.api.Assertions.*;

class ScraperUserServiceTest {

    private final String scraperServiceUrl = "http://localhost:8081";
    private ScraperUserService scraperUserService;
    private RestTemplateBuilder restTemplateBuilder;

    @BeforeEach
    void setUp() {
        restTemplateBuilder = new RestTemplateBuilder();
        scraperUserService = new ScraperUserService(restTemplateBuilder, scraperServiceUrl);
    }

    @Test
    void serviceCreation_Success() {
        // Given - servicio creado en setUp()

        // When & Then - Verificar que el servicio se crea correctamente
        assertNotNull(scraperUserService);
    }

    @Test
    void validateUser_WithNullParameters_ThrowsException() {
        // Given
        String email = null;
        String password = "password123";

        // When & Then
        assertThrows(Exception.class, () -> {
            scraperUserService.validateUser(email, password);
        });
    }

    @Test
    void registerUser_WithNullRequest_ThrowsException() {
        // Given
        RegisterRequest request = null;

        // When & Then
        assertThrows(Exception.class, () -> {
            scraperUserService.registerUser(request);
        });
    }

    @Test
    void constructor_InitializesCorrectly() {
        // Given
        RestTemplateBuilder builder = new RestTemplateBuilder();
        String url = "http://test-url:8080";

        // When
        ScraperUserService service = new ScraperUserService(builder, url);

        // Then
        assertNotNull(service);
    }

    @Test
    void constructor_WithNullBuilder_ThrowsException() {
        // Given
        RestTemplateBuilder builder = null;
        String url = "http://test-url:8080";

        // When & Then
        assertThrows(Exception.class, () -> {
            new ScraperUserService(builder, url);
        });
    }

    @Test
    void constructor_WithNullUrl_ThrowsException() {
        // Given
        RestTemplateBuilder builder = new RestTemplateBuilder();
        String url = null;

        // When & Then
        assertThrows(Exception.class, () -> {
            new ScraperUserService(builder, url);
        });
    }

}