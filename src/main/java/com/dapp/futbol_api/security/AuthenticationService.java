package com.dapp.futbol_api.security;

import com.dapp.futbol_api.service.ScraperUserService;
import com.dapp.futbol_api.utils.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final ScraperUserService scraperUserService;
    private final JwtService jwtService;
    private final UserValidator userValidator;

    public String register(RegisterRequest request) {
        userValidator.validateRegistrationRequest(request);
        return scraperUserService.registerUser(request);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        userValidator.validateAuthenticationRequest(request);

        // Validar contra scraper-service
        boolean isValidUser = scraperUserService.validateUser(request.getEmail(), request.getPassword());

        if (!isValidUser) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        // Crear UserDetails temporal y generar token directamente
        UserDetails userDetails = createTemporaryUserDetails(request.getEmail());
        var jwtToken = jwtService.generateToken(userDetails);

        return AuthenticationResponse.builder().token(jwtToken).build();
    }

    private UserDetails createTemporaryUserDetails(String email) {
        return User.builder()
                .username(email)
                .password("") // Esto está bien para JWT
                .authorities(Collections.emptyList())
                .build();
    }
}