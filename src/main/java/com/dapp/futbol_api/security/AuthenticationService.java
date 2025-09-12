package com.dapp.futbol_api.security;

import com.dapp.futbol_api.model.Role;
import com.dapp.futbol_api.model.User;
import com.dapp.futbol_api.repositories.UserRepository;
import com.dapp.futbol_api.utils.UserValidator;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserValidator userValidator;

    public String register(RegisterRequest request) {
        // Delegamos la validación al UserValidator
        userValidator.validateRegistrationRequest(request);

        var user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER) // Por defecto, rol USER
                .build();
        repository.save(user);
        return "Successfully registered!";
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        // Si llega aquí, el usuario está autenticado
        var user = repository.findByEmail(request.getEmail()).orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder().token(jwtToken).build();
    }
}
