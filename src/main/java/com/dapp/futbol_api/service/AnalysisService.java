package com.dapp.futbol_api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class AnalysisService extends AbstractWebService {

    private static final Logger log = LoggerFactory.getLogger(AnalysisService.class);
    private final String scraperServiceUrl;

    public AnalysisService(RestTemplateBuilder restTemplateBuilder,
            @Value("${scraper.service.url}") String scraperServiceUrl) {
        super(restTemplateBuilder, scraperServiceUrl);
        this.scraperServiceUrl = scraperServiceUrl;
    }

    /**
     * Gets performance metrics for a player.
     */
    public Object getPlayerMetrics(String playerName) {
        String decodedPlayerName = decodeUrlParameter(playerName);
        log.info("Requesting performance metrics for player '{}' from {}", decodedPlayerName, scraperServiceUrl);

        try {
            String url = UriComponentsBuilder.fromUriString(scraperServiceUrl)
                    .path("/api/analysis/{player}/metrics")
                    .buildAndExpand(encodePathSegment(decodedPlayerName))
                    .toUriString();

            log.debug("Calling metrics URL: {}", url);

            // Manejar tanto array como objeto individual
            Object response = restTemplate.getForObject(url, Object.class);

            // Si es una lista, extraer el primer elemento
            if (response instanceof List) {
                List<?> list = (List<?>) response;
                return list.isEmpty() ? null : list.get(0);
            }

            return response;
        } catch (HttpClientErrorException.NotFound e) {
            log.debug("Player '{}' not found for metrics analysis. Status: {}", decodedPlayerName, e.getStatusCode());
            throw new IllegalArgumentException("Player with name '" + decodedPlayerName + "' not found for analysis.",
                    e);
        } catch (HttpClientErrorException e) {
            log.debug("Client error fetching metrics for player '{}'. Status: {}, Body: {}", decodedPlayerName, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException(
                    "Error communicating with analysis service for player '" + decodedPlayerName + "'.", e);
        } catch (Exception e) {
            log.error("Unexpected error fetching metrics for '{}'", decodedPlayerName, e);
            throw new RuntimeException("Unexpected error while fetching player metrics.", e);
        }
    }

    /**
     * Gets performance prediction for the next match.
     */
    public Object getPerformancePrediction(String playerName, String opponent, boolean isHome, String position) {
        String decodedPlayerName = decodeUrlParameter(playerName);
        String decodedOpponent = decodeUrlParameter(opponent);
        String decodedPosition = decodeUrlParameter(position);

        log.info("Requesting performance prediction for player '{}' vs '{}' from {}",
                decodedPlayerName, decodedOpponent, scraperServiceUrl);

        try {
            String url = UriComponentsBuilder.fromUriString(scraperServiceUrl)
                    .path("/api/analysis/{player}/prediction")
                    .queryParam("opponent", encodeQueryParam(decodedOpponent))
                    .queryParam("isHome", isHome)
                    .queryParam("position", encodeQueryParam(decodedPosition))
                    .buildAndExpand(encodePathSegment(decodedPlayerName))
                    .toUriString();

            log.debug("Calling prediction URL: {}", url);

            // Manejar tanto array como objeto individual
            Object response = restTemplate.getForObject(url, Object.class);

            // Si es una lista, extraer el primer elemento
            if (response instanceof List) {
                List<?> list = (List<?>) response;
                return list.isEmpty() ? null : list.get(0);
            }

            return response;
        } catch (HttpClientErrorException.NotFound e) {
            log.debug("Player '{}' not found for prediction. Status: {}", decodedPlayerName, e.getStatusCode());
            throw new IllegalArgumentException("Player with name '" + decodedPlayerName + "' not found for prediction.",
                    e);
        } catch (HttpClientErrorException e) {
            log.debug("Client error fetching prediction for player '{}'. Status: {}, Body: {}", decodedPlayerName, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Error generating prediction for player '" + decodedPlayerName + "'.", e);
        } catch (Exception e) {
            log.error("Unexpected error generating prediction for '{}'", decodedPlayerName, e);
            throw new RuntimeException("Unexpected error while generating performance prediction.", e);
        }
    }

    /**
     * Encodes a query parameter for a URL.
     */
    private String encodeQueryParam(String param) {
        return UriUtils.encodeQueryParam(param, StandardCharsets.UTF_8);
    }

    /**
     * Converts scraped data to analysis format.
     */
    public Object convertPlayerData(String playerName) {
        String decodedPlayerName = decodeUrlParameter(playerName);
        log.info("Converting player data for '{}' to analysis format using {}", decodedPlayerName, scraperServiceUrl);

        try {
            String url = UriComponentsBuilder.fromUriString(scraperServiceUrl)
                    .path("/api/analysis/{player}/convert-data")
                    .buildAndExpand(encodePathSegment(decodedPlayerName))
                    .toUriString();

            log.debug("Calling convert-data URL: {}", url);

            Object result = restTemplate.postForObject(url, null, Object.class);
            return result != null ? result : Map.of("message", "Conversion completed for " + decodedPlayerName);
        } catch (HttpClientErrorException.NotFound e) {
            log.debug("Player '{}' not found for data conversion. Status: {}", decodedPlayerName, e.getStatusCode());
            throw new IllegalArgumentException("Player with name '" + decodedPlayerName + "' not found for conversion.",
                    e);
        } catch (Exception e) {
            log.error("Error converting data for '{}'", decodedPlayerName, e);
            throw new RuntimeException("Error converting player data to analysis format.", e);
        }
    }

    /**
     * Gets a comparative analysis of the player.
     */
    public Object getComparativeAnalysis(String playerName) {
        String decodedPlayerName = decodeUrlParameter(playerName);
        log.info("Requesting comparative analysis for player '{}' from {}", decodedPlayerName, scraperServiceUrl);

        try {
            String url = UriComponentsBuilder.fromUriString(scraperServiceUrl)
                    .path("/api/analysis/{player}/comparison")
                    .buildAndExpand(encodePathSegment(decodedPlayerName))
                    .toUriString();

            log.debug("Calling comparison URL: {}", url);

            // Manejar tanto array como objeto individual
            Object response = restTemplate.getForObject(url, Object.class);

            // Si es una lista, extraer el primer elemento
            if (response instanceof List) {
                List<?> list = (List<?>) response;
                return list.isEmpty() ? null : list.get(0);
            }

            return response;
        } catch (Exception e) {
            log.error("Error fetching comparative analysis for '{}'", decodedPlayerName, e);
            throw new RuntimeException("Error fetching comparative analysis.", e);
        }
    }

    /**
     * Decodes URL parameters (converts %20 to spaces, etc.).
     */
    private String decodeUrlParameter(String encodedValue) {
        try {
            return URLDecoder.decode(encodedValue, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            log.warn("Failed to decode URL parameter '{}', using original value", encodedValue);
            return encodedValue;
        }
    }

    /**
     * Encodes a path segment for a URL (handles spaces, special characters, etc.).
     */
    private String encodePathSegment(String segment) {
        return UriUtils.encodePathSegment(segment, StandardCharsets.UTF_8);
    }
}