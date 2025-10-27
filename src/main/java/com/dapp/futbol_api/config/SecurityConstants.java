package com.dapp.futbol_api.config;

public final class SecurityConstants {

    public static final String[] WHITE_LIST_URLS = {
            // Root endpoint
            "/",
            // Public authentication endpoints
            "/api/auth/**",
            // Documentation endpoints (Swagger)
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            // Path for Render's Health Check
            "/actuator/**",
            // Public scraping endpoints
            "/api/player",
            "/api/player/**",
            "/api/player*",
            "/api/team",
            "/api/team/**",
            "/api/team*",
            "/api/analysis/**",
            "/api/futureMatches"
    };

}