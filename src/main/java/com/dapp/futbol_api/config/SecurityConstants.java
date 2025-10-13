package com.dapp.futbol_api.config;

public final class SecurityConstants {

    public static final String[] WHITE_LIST_URLS = {
            // Endpoint raíz
            "/",
            // Endpoints públicos de autenticación
            "/api/auth/**",
            // Endpoints de documentación (Swagger)
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            // Ruta para el Health Check de Render
            "/actuator/**",
            // Endpoints de scraping que son públicos
            "/api/player",
            "/api/team"
    };

    private SecurityConstants() {
    }
}