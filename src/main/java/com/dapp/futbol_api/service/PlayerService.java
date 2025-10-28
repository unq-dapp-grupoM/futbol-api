package com.dapp.futbol_api.service;

import com.dapp.futbol_api.exception.PlayerServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Map;

@Service
public class PlayerService extends AbstractWebService {

    private static final Logger log = LoggerFactory.getLogger(PlayerService.class);

    public PlayerService(RestTemplateBuilder restTemplateBuilder,
            @Value("${scraper.service.url}") String scraperServiceUrl) {
        super(restTemplateBuilder, scraperServiceUrl);
    }

    public Object getPlayerInfoByName(String playerName) {
        log.info("Requesting player info for '{}' from scraper service", playerName);

        try {
            String url = buildPlayerUrl(playerName);
            log.debug("Final URL to scraper-service: {}", url);

            // Get as a list and extract the first element
            List<Map<String, Object>> playersList = restTemplate.getForObject(url, List.class);

            if (playersList == null || playersList.isEmpty()) {
                throw new IllegalArgumentException("Player with name '" + playerName + "' not found.");
            }

            return playersList.get(0);
        } catch (HttpClientErrorException.NotFound e) {
            log.debug("Player '{}' not found by scraper service. Status: {}", playerName, e.getStatusCode());
            throw new IllegalArgumentException("Player with name '" + playerName + "' not found.", e);
        } catch (Exception e) {
            log.error("Error fetching player data for '{}'", playerName, e);
            throw new PlayerServiceException("Error fetching player data.", e);
        }
    }

    /**
     * Builds the URL manually to avoid double encoding.
     */
    private String buildPlayerUrl(String playerName) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append("/api/scrape/player?playerName=");

        urlBuilder.append(encodeValue(playerName));

        return urlBuilder.toString();
    }

    /**
     * Simple manual encoding for spaces and other characters.
     */
    private String encodeValue(String value) {
        return value.replace(" ", "%20")
                .replace("&", "%26")
                .replace("?", "%3F")
                .replace("=", "%3D");
    }
}