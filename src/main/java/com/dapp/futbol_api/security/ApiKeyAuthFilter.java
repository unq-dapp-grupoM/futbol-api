package com.dapp.futbol_api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.util.Arrays;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    @Value("${api.security.key}")
    private String principalRequestHeader;

    // Replicamos la lista blanca de SecurityConfig para que este filtro también la
    // conozca.
    private static final String[] WHITE_LIST_URLS = {
            "/",
            "/api/auth/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/actuator/**",
            "/api/player",
            "/api/team"
    };

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // Si la petición coincide con alguna de las URLs de la lista blanca,
        // saltamos este filtro y continuamos con la cadena.
        if (isWhiteListed(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ Si ya hay autenticación, continuar
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 🔑 Verificar API Key
        String potentialApiKey = request.getHeader("X-API-KEY");

        if (potentialApiKey != null && potentialApiKey.equals(principalRequestHeader)) {
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    "api-service",
                    null,
                    AuthorityUtils.createAuthorityList("ROLE_SERVICE"));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private boolean isWhiteListed(HttpServletRequest request) {
        return Arrays.stream(WHITE_LIST_URLS)
                .anyMatch(pattern -> {
                    AntPathRequestMatcher matcher = new AntPathRequestMatcher(pattern);
                    return matcher.matches(request);
                });
    }
}
