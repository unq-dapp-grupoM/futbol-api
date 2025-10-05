package com.dapp.futbol_api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OpenApiConfigTest {

    @Test
    void testOpenApiDefinitionAnnotationIsPresentAndCorrectlyConfigured() {
        // Arrange
        Class<OpenApiConfig> configClass = OpenApiConfig.class;

        // Act
        OpenAPIDefinition apiDefinition = configClass.getAnnotation(OpenAPIDefinition.class);

        // Assert
        assertNotNull(apiDefinition, "The @OpenAPIDefinition annotation should be present.");

        // Assert Info details
        Info info = apiDefinition.info();
        assertNotNull(info, "The 'info' attribute of @OpenAPIDefinition should be configured.");
        assertEquals("Futbol API", info.title());
        assertEquals("API for football data management.", info.description());
        assertEquals("1.0.0", info.version());

        // Assert Security Requirement details
        SecurityRequirement[] securityRequirements = apiDefinition.security();
        assertEquals(1, securityRequirements.length, "There should be one security requirement.");
        assertEquals("bearerAuth", securityRequirements[0].name());
    }

    @Test
    void testSecuritySchemeAnnotationIsPresentAndCorrectlyConfigured() {
        // Arrange
        Class<OpenApiConfig> configClass = OpenApiConfig.class;

        // Act
        SecurityScheme securityScheme = configClass.getAnnotation(SecurityScheme.class);

        // Assert
        assertNotNull(securityScheme, "The @SecurityScheme annotation should be present.");

        // Assert Security Scheme details
        assertEquals("bearerAuth", securityScheme.name());
        assertEquals("JWT auth", securityScheme.description());
        assertEquals("bearer", securityScheme.scheme());
        assertEquals(SecuritySchemeType.HTTP, securityScheme.type());
        assertEquals("JWT", securityScheme.bearerFormat());
        assertEquals(SecuritySchemeIn.HEADER, securityScheme.in());
    }
}
