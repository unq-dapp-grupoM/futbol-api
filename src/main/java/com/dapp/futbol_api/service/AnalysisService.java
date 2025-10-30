package com.dapp.futbol_api.service;

import com.dapp.futbol_api.exception.AnalysisServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpMethod;
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
    public Object getPlayerPerformanceMetrics(String playerName) {
        String decodedPlayerName = decodeUrlParameter(playerName);
        log.info("Requesting performance metrics for player '{}' from {}", decodedPlayerName, scraperServiceUrl);

        String url = UriComponentsBuilder.fromUriString(scraperServiceUrl)
                .path("/api/analysis/{player}/performanceMetrics")
                .buildAndExpand(encodePathSegment(decodedPlayerName))
                .toUriString();

        return performApiCall(url, HttpMethod.GET, decodedPlayerName, "performance metrics analysis",
                "Unexpected error while fetching player performance metrics.");
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

        String url = UriComponentsBuilder.fromUriString(scraperServiceUrl)
                .path("/api/analysis/{player}/prediction")
                .queryParam("opponent", encodeQueryParam(decodedOpponent))
                .queryParam("isHome", isHome)
                .queryParam("position", encodeQueryParam(decodedPosition))
                .buildAndExpand(encodePathSegment(decodedPlayerName))
                .toUriString();

        return performApiCall(url, HttpMethod.GET, decodedPlayerName, "prediction",
                "Unexpected error while generating performance prediction.");
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

        String url = UriComponentsBuilder.fromUriString(scraperServiceUrl)
                .path("/api/analysis/{player}/convert-data")
                .buildAndExpand(encodePathSegment(decodedPlayerName))
                .toUriString();

        return performApiCall(url, HttpMethod.POST, decodedPlayerName, "data conversion",
                "Error converting player data to analysis format.");
    }

    /**
     * Gets a comparative analysis of the player.
     */
    public Object getComparativeAnalysis(String playerName) {
        String decodedPlayerName = decodeUrlParameter(playerName);
        log.info("Requesting comparative analysis for player '{}' from {}", decodedPlayerName, scraperServiceUrl);

        String url = UriComponentsBuilder.fromUriString(scraperServiceUrl)
                .path("/api/analysis/{player}/comparison")
                .buildAndExpand(encodePathSegment(decodedPlayerName))
                .toUriString();

        return performApiCall(url, HttpMethod.GET, decodedPlayerName, "comparative analysis",
                "Error fetching comparative analysis.");
    }

    /**
     * Performs the actual API call and handles common responses and exceptions.
     */
    private Object performApiCall(String url, HttpMethod method, String playerName, String operation, String errorMsg) {
        log.debug("Calling URL for {}: {}", operation, url);
        try {
            Object response;
            if (method == HttpMethod.POST) {
                response = restTemplate.postForObject(url, null, Object.class);
                if (response == null) {
                    return Map.of("message", "Operation completed for " + playerName);
                }
            } else { // Default to GET
                response = restTemplate.getForObject(url, Object.class);
            }

            if (response instanceof List) {
                List<?> list = (List<?>) response;
                return list.isEmpty() ? null : list.get(0);
            }
            return response;

        } catch (HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException(
                    "Player with name '" + playerName + "' not found for " + operation + ".", e);

        } catch (HttpClientErrorException e) {
            throw new AnalysisServiceException(
                    "Error communicating with analysis service for player '" + playerName + "'.", e);

        } catch (Exception e) {
            throw new AnalysisServiceException(errorMsg, e);
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