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

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(AbstractHttpConfigurer::disable)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                // ✅ Aseguramos que los filtros no se apliquen a las rutas públicas
                                .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class)
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                                .authorizeHttpRequests(req -> req
                                                // 1. Rutas públicas: no requieren ningún filtro ni autenticación.
                                                .requestMatchers(SecurityConstants.WHITE_LIST_URLS).permitAll()
                                                // 2. Rutas de servicio: requieren el ApiKeyAuthFilter (y rol SERVICE).
                                                .requestMatchers("/api/v1/internal/**").hasRole("SERVICE")
                                                // 3. El resto de rutas: requieren el JwtAuthenticationFilter (y estar
                                                // autenticado).
                                                .anyRequest().authenticated());
                return http.build();
        }
}
