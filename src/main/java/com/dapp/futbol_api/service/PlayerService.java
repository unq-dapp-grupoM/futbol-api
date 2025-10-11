package com.dapp.futbol_api.service;

import com.dapp.futbol_api.model.dto.PlayerDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class PlayerService extends AbstractWebService {

    private static final Logger log = LoggerFactory.getLogger(PlayerService.class);

    public PlayerService(RestTemplateBuilder restTemplateBuilder,
            @Value("${scraper.service.url}") String scraperServiceUrl) {
        // La URL del scraper service se inyectará desde application.properties
        super(restTemplateBuilder, scraperServiceUrl);
    }

    public PlayerDTO getPlayerInfoByName(String playerName) {
        log.info("Requesting player info for '{}' from scraper service", playerName);
        try {
            String url = UriComponentsBuilder.fromPath("/api/scrape/player")
                    .queryParam("playerName", playerName)
                    .toUriString();

            PlayerDTO playerDTO = restTemplate.getForObject(url, PlayerDTO.class);

            if (playerDTO == null) {
                throw new IllegalArgumentException("Player with name '" + playerName + "' not found.");
            }
            return playerDTO;
        } catch (HttpClientErrorException.NotFound e) {
            log.error("Player '{}' not found by scraper service. Status: {}", playerName, e.getStatusCode());
            throw new IllegalArgumentException("Player with name '" + playerName + "' not found.", e);
        } catch (HttpClientErrorException e) {
            log.error("Scraper service returned an error for player '{}'. Status: {}, Body: {}", playerName,
                    e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new RuntimeException("Error communicating with scraper service for player '" + playerName + "'.", e);
        } catch (Exception e) {
            log.error("An unexpected error occurred while fetching player data for '{}': {}", playerName,
                    e.getMessage(), e);
            throw new RuntimeException("An unexpected error occurred while fetching player data.", e);
        }
    }
}
