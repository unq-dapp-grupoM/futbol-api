package com.dapp.futbol_api.service;

import com.dapp.futbol_api.model.dto.TeamDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class TeamService extends AbstractWebService {

    private static final Logger log = LoggerFactory.getLogger(TeamService.class);

    public TeamDTO getTeamInfoByName(String teamName) {
        log.info("Requesting team info for '{}' from scraper service", teamName);
        try {
            String url = UriComponentsBuilder.fromPath("/api/scrape/team")
                    .queryParam("teamName", teamName)
                    .toUriString();

            TeamDTO teamDTO = restTemplate.getForObject(url, TeamDTO.class);

            if (teamDTO == null) {
                throw new IllegalArgumentException("Team with name '" + teamName + "' not found.");
            }
            return teamDTO;
        } catch (HttpClientErrorException.NotFound e) {
            log.error("Team '{}' not found by scraper service. Status: {}", teamName, e.getStatusCode());
            throw new IllegalArgumentException("Team with name '" + teamName + "' not found.", e);
        } catch (HttpClientErrorException e) {
            log.error("Scraper service returned an error for team '{}'. Status: {}, Body: {}", teamName,
                    e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new RuntimeException("Error communicating with scraper service for team '" + teamName + "'.", e);
        } catch (Exception e) {
            log.error("An unexpected error occurred while fetching team data for '{}': {}", teamName, e.getMessage(),
                    e);
            throw new RuntimeException("An unexpected error occurred while fetching team data.", e);
        }
    }

    public TeamService(RestTemplateBuilder restTemplateBuilder,
            @Value("${scraper.service.url}") String scraperServiceUrl) {
        // La URL del scraper service se inyectará desde application.properties
        super(restTemplateBuilder, scraperServiceUrl);
    }
}
