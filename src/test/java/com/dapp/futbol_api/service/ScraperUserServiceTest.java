package com.dapp.futbol_api.service;

import com.dapp.futbol_api.security.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScraperUserServiceTest {

    private RestTemplateBuilder restTemplateBuilder;
    private RestTemplate restTemplate;
    private ScraperUserService scraperUserService;

    private final String scraperServiceUrl = "http://localhost:8081";

    // Tests para registerUser
    @Test
    void registerUser_Success() {
        // Usar MockedConstruction para el DefaultUriBuilderFactory
        try (MockedConstruction<DefaultUriBuilderFactory> ignored =
                     mockConstruction(DefaultUriBuilderFactory.class)) {

            // Configurar el builder mock
            RestTemplate mockRestTemplate = mock(RestTemplate.class);
            RestTemplateBuilder mockBuilder = mock(RestTemplateBuilder.class);

            when(mockBuilder.uriTemplateHandler(any(DefaultUriBuilderFactory.class)))
                    .thenReturn(mockBuilder);
            when(mockBuilder.build())
                    .thenReturn(mockRestTemplate);

            ScraperUserService service = new ScraperUserService(mockBuilder, scraperServiceUrl);

            // Given
            RegisterRequest request = new RegisterRequest();
            request.setEmail("test@example.com");
            request.setPassword("password123");

            String expectedResponse = "User registered successfully";
            ResponseEntity<String> responseEntity = ResponseEntity.ok(expectedResponse);

            when(mockRestTemplate.postForEntity(
                    eq("/api/scrape/auth/register"),
                    any(Map.class),
                    eq(String.class)
            )).thenReturn(responseEntity);

            // When
            String result = service.registerUser(request);

            // Then
            assertEquals(expectedResponse, result);
            verify(mockRestTemplate).postForEntity(
                    eq("/api/scrape/auth/register"),
                    argThat((Map<String, Object> body) ->
                            body.get("email").equals("test@example.com") &&
                                    body.get("password").equals("password123") &&
                                    body.get("role").equals("USER")
                    ),
                    eq(String.class)
            );
        }
    }

    @Test
    void registerUser_UserAlreadyExists() {
        try (MockedConstruction<DefaultUriBuilderFactory> ignored =
                     mockConstruction(DefaultUriBuilderFactory.class)) {

            RestTemplate mockRestTemplate = mock(RestTemplate.class);
            RestTemplateBuilder mockBuilder = mock(RestTemplateBuilder.class);

            when(mockBuilder.uriTemplateHandler(any(DefaultUriBuilderFactory.class)))
                    .thenReturn(mockBuilder);
            when(mockBuilder.build())
                    .thenReturn(mockRestTemplate);

            ScraperUserService service = new ScraperUserService(mockBuilder, scraperServiceUrl);

            // Given
            RegisterRequest request = new RegisterRequest();
            request.setEmail("existing@example.com");
            request.setPassword("password123");

            when(mockRestTemplate.postForEntity(
                    anyString(),
                    any(Map.class),
                    eq(String.class)
            )).thenThrow(HttpClientErrorException.BadRequest.create(
                    HttpStatus.BAD_REQUEST,
                    "Bad Request",
                    null, null, null
            ));

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                service.registerUser(request);
            });

            assertEquals("User already exists with this email", exception.getMessage());
        }
    }

    @Test
    void registerUser_ServiceCommunicationError() {
        try (MockedConstruction<DefaultUriBuilderFactory> ignored =
                     mockConstruction(DefaultUriBuilderFactory.class)) {

            RestTemplate mockRestTemplate = mock(RestTemplate.class);
            RestTemplateBuilder mockBuilder = mock(RestTemplateBuilder.class);

            when(mockBuilder.uriTemplateHandler(any(DefaultUriBuilderFactory.class)))
                    .thenReturn(mockBuilder);
            when(mockBuilder.build())
                    .thenReturn(mockRestTemplate);

            ScraperUserService service = new ScraperUserService(mockBuilder, scraperServiceUrl);

            // Given
            RegisterRequest request = new RegisterRequest();
            request.setEmail("test@example.com");
            request.setPassword("password123");

            when(mockRestTemplate.postForEntity(
                    anyString(),
                    any(Map.class),
                    eq(String.class)
            )).thenThrow(new RestClientException("Connection failed"));

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                service.registerUser(request);
            });

            assertEquals("Error communicating with user service", exception.getMessage());
            assertInstanceOf(RestClientException.class, exception.getCause());
        }
    }

    // Tests para validateUser
    @Test
    void validateUser_Success_ValidUser() {
        try (MockedConstruction<DefaultUriBuilderFactory> ignored =
                     mockConstruction(DefaultUriBuilderFactory.class)) {

            RestTemplate mockRestTemplate = mock(RestTemplate.class);
            RestTemplateBuilder mockBuilder = mock(RestTemplateBuilder.class);

            when(mockBuilder.uriTemplateHandler(any(DefaultUriBuilderFactory.class)))
                    .thenReturn(mockBuilder);
            when(mockBuilder.build())
                    .thenReturn(mockRestTemplate);

            ScraperUserService service = new ScraperUserService(mockBuilder, scraperServiceUrl);

            // Given
            String email = "test@example.com";
            String password = "password123";
            Map<String, Object> responseBody = Map.of("valid", true);
            ResponseEntity<Map<String, Object>> responseEntity = ResponseEntity.ok(responseBody);

            when(mockRestTemplate.exchange(
                    eq("/api/scrape/auth/validate"),
                    eq(HttpMethod.POST),
                    isNull(),
                    any(ParameterizedTypeReference.class),
                    any(Map.class)
            )).thenReturn(responseEntity);

            // When
            boolean result = service.validateUser(email, password);

            // Then
            assertTrue(result);
        }
    }

    @Test
    void validateUser_Success_InvalidUser() {
        try (MockedConstruction<DefaultUriBuilderFactory> ignored =
                     mockConstruction(DefaultUriBuilderFactory.class)) {

            RestTemplate mockRestTemplate = mock(RestTemplate.class);
            RestTemplateBuilder mockBuilder = mock(RestTemplateBuilder.class);

            when(mockBuilder.uriTemplateHandler(any(DefaultUriBuilderFactory.class)))
                    .thenReturn(mockBuilder);
            when(mockBuilder.build())
                    .thenReturn(mockRestTemplate);

            ScraperUserService service = new ScraperUserService(mockBuilder, scraperServiceUrl);

            // Given
            String email = "test@example.com";
            String password = "wrongpassword";
            Map<String, Object> responseBody = Map.of("valid", false);
            ResponseEntity<Map<String, Object>> responseEntity = ResponseEntity.ok(responseBody);

            when(mockRestTemplate.exchange(
                    eq("/api/scrape/auth/validate"),
                    eq(HttpMethod.POST),
                    isNull(),
                    any(ParameterizedTypeReference.class),
                    any(Map.class)
            )).thenReturn(responseEntity);

            // When
            boolean result = service.validateUser(email, password);

            // Then
            assertFalse(result);
        }
    }

    @Test
    void validateUser_NullResponseBody() {
        try (MockedConstruction<DefaultUriBuilderFactory> ignored =
                     mockConstruction(DefaultUriBuilderFactory.class)) {

            RestTemplate mockRestTemplate = mock(RestTemplate.class);
            RestTemplateBuilder mockBuilder = mock(RestTemplateBuilder.class);

            when(mockBuilder.uriTemplateHandler(any(DefaultUriBuilderFactory.class)))
                    .thenReturn(mockBuilder);
            when(mockBuilder.build())
                    .thenReturn(mockRestTemplate);

            ScraperUserService service = new ScraperUserService(mockBuilder, scraperServiceUrl);

            // Given
            String email = "test@example.com";
            String password = "password123";
            ResponseEntity<Map<String, Object>> responseEntity = ResponseEntity.ok(null);

            when(mockRestTemplate.exchange(
                    eq("/api/scrape/auth/validate"),
                    eq(HttpMethod.POST),
                    isNull(),
                    any(ParameterizedTypeReference.class),
                    any(Map.class)
            )).thenReturn(responseEntity);

            // When
            boolean result = service.validateUser(email, password);

            // Then
            assertFalse(result);
        }
    }

    @Test
    void validateUser_Non2xxStatusCode() {
        try (MockedConstruction<DefaultUriBuilderFactory> ignored =
                     mockConstruction(DefaultUriBuilderFactory.class)) {

            RestTemplate mockRestTemplate = mock(RestTemplate.class);
            RestTemplateBuilder mockBuilder = mock(RestTemplateBuilder.class);

            when(mockBuilder.uriTemplateHandler(any(DefaultUriBuilderFactory.class)))
                    .thenReturn(mockBuilder);
            when(mockBuilder.build())
                    .thenReturn(mockRestTemplate);

            ScraperUserService service = new ScraperUserService(mockBuilder, scraperServiceUrl);

            // Given
            String email = "test@example.com";
            String password = "password123";
            Map<String, Object> responseBody = Map.of("valid", true);
            ResponseEntity<Map<String, Object>> responseEntity =
                    new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);

            when(mockRestTemplate.exchange(
                    eq("/api/scrape/auth/validate"),
                    eq(HttpMethod.POST),
                    isNull(),
                    any(ParameterizedTypeReference.class),
                    any(Map.class)
            )).thenReturn(responseEntity);

            // When
            boolean result = service.validateUser(email, password);

            // Then
            assertFalse(result);
        }
    }

    @Test
    void validateUser_ServiceCommunicationError() {
        try (MockedConstruction<DefaultUriBuilderFactory> ignored =
                     mockConstruction(DefaultUriBuilderFactory.class)) {

            RestTemplate mockRestTemplate = mock(RestTemplate.class);
            RestTemplateBuilder mockBuilder = mock(RestTemplateBuilder.class);

            when(mockBuilder.uriTemplateHandler(any(DefaultUriBuilderFactory.class)))
                    .thenReturn(mockBuilder);
            when(mockBuilder.build())
                    .thenReturn(mockRestTemplate);

            ScraperUserService service = new ScraperUserService(mockBuilder, scraperServiceUrl);

            // Given
            String email = "test@example.com";
            String password = "password123";

            when(mockRestTemplate.exchange(
                    eq("/api/scrape/auth/validate"),
                    eq(HttpMethod.POST),
                    isNull(),
                    any(ParameterizedTypeReference.class),
                    any(Map.class)
            )).thenThrow(new RestClientException("Validation service unavailable"));

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                service.validateUser(email, password);
            });

            assertEquals("Error validating user credentials", exception.getMessage());
            assertInstanceOf(RestClientException.class, exception.getCause());
        }
    }
}