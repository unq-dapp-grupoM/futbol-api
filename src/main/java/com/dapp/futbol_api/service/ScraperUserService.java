package com.dapp.futbol_api.service;

import com.dapp.futbol_api.security.RegisterRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Service
public class ScraperUserService extends AbstractWebService {

    public ScraperUserService(RestTemplateBuilder restTemplateBuilder,
            @Value("${scraper.service.url}") String scraperServiceUrl) {
        super(restTemplateBuilder, scraperServiceUrl);
    }

    public String registerUser(RegisterRequest request) {
        try {
            String url = "/api/scrape/auth/register";

            Map<String, Object> requestBody = Map.of(
                    "email", request.getEmail(),
                    "password", request.getPassword(), // Contraseña SIN encriptar
                    "role", "USER");

            System.out.println("DEBUG: Registering user - Email: " + request.getEmail());

            ResponseEntity<String> response = restTemplate.postForEntity(url, requestBody, String.class);
            String result = response.getBody();

            System.out.println("DEBUG: Registration result: " + result);
            return result;

        } catch (HttpClientErrorException.BadRequest e) {
            throw new IllegalArgumentException("User already exists with this email", e);
        } catch (RestClientException e) {
            System.out.println("DEBUG: Registration error: " + e.getMessage());
            throw new RuntimeException("Error communicating with user service", e);
        }
    }

    public boolean validateUser(String email, String password) {
        try {
            String url = "/api/scrape/auth/validate";

            Map<String, String> requestBody = Map.of(
                    "email", email,
                    "password", password // Contraseña SIN encriptar
            );

            System.out.println("DEBUG: Validating user in scraper-service - Email: " + email);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestBody, Map.class);

            System.out.println("DEBUG: Validation response: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                boolean isValid = Boolean.TRUE.equals(response.getBody().get("valid"));
                System.out.println("DEBUG: User validation result: " + isValid);
                return isValid;
            }

            return false;

        } catch (RestClientException e) {
            System.out.println("DEBUG: Validation error: " + e.getMessage());
            throw new RuntimeException("Error validating user credentials", e);
        }
    }
}