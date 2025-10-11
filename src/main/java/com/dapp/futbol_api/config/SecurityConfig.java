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
                        // Rutas públicas de la API
                        "/api/**",
                        // Rutas de documentación (Swagger)
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        // Ruta para el Health Check de Render
                        "/actuator/**"
        };

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(AbstractHttpConfigurer::disable)
                                .authorizeHttpRequests(req -> req.requestMatchers(WHITE_LIST_URLS).permitAll()
                                                // Ejemplo: un endpoint protegido por API Key
                                                .requestMatchers("/api/v1/internal/**").hasRole("SERVICE")
                                                // El resto de endpoints requieren autenticación JWT
                                                .anyRequest().authenticated())

                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                // Primero se ejecuta el filtro de API Key para las rutas internas
                                .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class)
                                // Luego, el filtro JWT para el resto de las rutas autenticadas
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}
