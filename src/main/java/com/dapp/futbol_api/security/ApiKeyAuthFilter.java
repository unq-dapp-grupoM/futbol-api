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

        // If the request matches any of the whitelisted URLs,
        // we skip this filter and continue with the chain.
        if (isWhiteListed(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // If there is already an authentication, continue
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Verify API Key
        String potentialApiKey = request.getHeader("X-API-KEY");

        if (potentialApiKey != null && potentialApiKey.equals(principalRequestHeader)) {
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    "api-service",
                    null,
                    AuthorityUtils.createAuthorityList("ROLE_SERVICE"));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            log.warn("Invalid or missing API Key for: {}", request.getRequestURI());
        }

        filterChain.doFilter(request, response);
    }

    private boolean isWhiteListed(HttpServletRequest request) {
        return Arrays.stream(SecurityConstants.WHITE_LIST_URLS)
                .anyMatch(pattern -> new AntPathRequestMatcher(pattern).matches(request));
    }
}
