package com.dapp.futbol_api.config;

import com.dapp.futbol_api.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor

public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthFilter;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(AbstractHttpConfigurer::disable)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(req -> req
                                                // 1. Public routes: do not require any filter or authentication.
                                                .requestMatchers(SecurityConstants.WHITE_LIST_URLS).permitAll()
                                                // 2. User routes: require JWT and USER authority.
                                                .requestMatchers(SecurityConstants.USER_LIST_URLS).hasAuthority("USER")
                                                // 3. Any other request: requires the JwtAuthenticationFilter (and to be authenticated).
                                                .anyRequest().authenticated())
                                // Add the filter AFTER defining the authorizations
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

                // Necessary for the H2 console if used within an iframe and a general good practice
                http.headers(headers -> headers.frameOptions(FrameOptionsConfig::sameOrigin));

                return http.build();
        }
}
