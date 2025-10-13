package com.dapp.futbol_api.config;

import com.dapp.futbol_api.security.ApiKeyAuthFilter;
import com.dapp.futbol_api.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor

public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthFilter;
        private final ApiKeyAuthFilter apiKeyAuthFilter;

        private static final String[] WHITE_LIST_URLS = {
                        // Endpoint raíz
                        "/",
                        // Endpoints públicos que no requieren ningún tipo de token
                        "/api/auth/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/actuator/**",
                        // Endpoints de scraping que son públicos
                        "/api/player",
                        "/api/team"
        };

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(AbstractHttpConfigurer::disable)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(req -> req
                                                // 1. Rutas públicas: no requieren ningún filtro ni autenticación.
                                                .requestMatchers(WHITE_LIST_URLS).permitAll()
                                                // 2. Rutas de servicio: requieren el ApiKeyAuthFilter (y rol SERVICE).
                                                .requestMatchers("/api/v1/internal/**").hasRole("SERVICE")
                                                // 3. El resto de rutas: requieren el JwtAuthenticationFilter (y estar
                                                // autenticado).
                                                .anyRequest().authenticated())
                                .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class)
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}
