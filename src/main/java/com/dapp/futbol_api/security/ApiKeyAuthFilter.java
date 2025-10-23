package com.dapp.futbol_api.security;

import com.dapp.futbol_api.config.SecurityConstants;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.util.Arrays;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyAuthFilter.class);

    @Value("${api.security.key}")
    private String principalRequestHeader;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        log.info("🔐 Processing request: {} {}", request.getMethod(), requestURI);

        // Si la petición coincide con alguna de las URLs de la lista blanca,
        // saltamos este filtro y continuamos con la cadena.
        if (isWhiteListed(request)) {
            log.info("✅ Whitelisted - Skipping auth for: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ Si ya hay autenticación, continuar
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            log.info("✅ Already authenticated - Continuing for: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // 🔑 Verificar API Key
        String potentialApiKey = request.getHeader("X-API-KEY");
        log.info("🔑 Checking API Key for: {}", requestURI);

        if (potentialApiKey != null && potentialApiKey.equals(principalRequestHeader)) {
            log.info("✅ API Key valid for: {}", requestURI);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    "api-service",
                    null,
                    AuthorityUtils.createAuthorityList("ROLE_SERVICE"));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            log.warn("❌ Invalid or missing API Key for: {}", requestURI);
        }

        filterChain.doFilter(request, response);
    }

    private boolean isWhiteListed(HttpServletRequest request) {
        boolean isWhiteListed = Arrays.stream(SecurityConstants.WHITE_LIST_URLS)
                .anyMatch(pattern -> {
                    AntPathRequestMatcher matcher = new AntPathRequestMatcher(pattern);
                    boolean matches = matcher.matches(request);
                    if (matches) {
                        log.info("🎯 Pattern '{}' matches request: {}", pattern, request.getRequestURI());
                    }
                    return matches;
                });

        log.info("📋 Whitelist check for {}: {}", request.getRequestURI(), isWhiteListed);
        return isWhiteListed;
    }
}
