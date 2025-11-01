package com.dapp.futbol_api.service;

import com.dapp.futbol_api.security.RegisterRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
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

            ResponseEntity<String> response = restTemplate.postForEntity(url, requestBody, String.class);
            String result = response.getBody();

            return result;

        } catch (HttpClientErrorException.BadRequest e) {
            throw new IllegalArgumentException("User already exists with this email", e);
        } catch (RestClientException e) {
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

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {},
                    requestBody
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                boolean isValid = Boolean.TRUE.equals(response.getBody().get("valid"));
                return isValid;
            }

            return false;

        } catch (RestClientException e) {
            throw new RuntimeException("Error validating user credentials", e);
        }
    }
}