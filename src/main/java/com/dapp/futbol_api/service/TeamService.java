package com.dapp.futbol_api.service;

import com.dapp.futbol_api.exception.TeamServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Map;

@Service
public class TeamService extends AbstractWebService {

    private static final Logger log = LoggerFactory.getLogger(TeamService.class);

    public TeamService(RestTemplateBuilder restTemplateBuilder,
            @Value("${scraper.service.url}") String scraperServiceUrl) {
        super(restTemplateBuilder, scraperServiceUrl);
    }

    public Object getTeamInfoByName(String teamName) {
        log.info("Requesting team info for '{}' from scraper service", teamName);

        try {
            String url = buildTeamUrl(teamName);
            log.debug("Final URL to scraper-service: {}", url);

            // Get as a list and extract the first element
            List<Map<String, Object>> teamsList = restTemplate.getForObject(url, List.class);

            if (teamsList == null || teamsList.isEmpty()) {
                throw new IllegalArgumentException("Team with name '" + teamName + "' not found.");
            }

            return teamsList.get(0);
        } catch (HttpClientErrorException.NotFound e) {
            log.debug("Team '{}' not found by scraper service. Status: {}", teamName, e.getStatusCode());
            throw new IllegalArgumentException("Team with name '" + teamName + "' not found.", e);
        } catch (Exception e) {
            log.error("Error fetching team data for '{}'", teamName, e);
            throw new TeamServiceException("Error fetching team data.", e);
        }
    }

    public Object getFutureMatches(String teamName) {
        log.info("Requesting future matches for '{}' from scraper service", teamName);

        try {
            String url = buildFutureMatchesUrl(teamName);
            log.debug("Final URL to scraper-service for future matches: {}", url);

            // Get the list of matches
            return restTemplate.getForObject(url, List.class);

        } catch (HttpClientErrorException.NotFound e) {
            log.debug("Team '{}' not found by scraper service for future matches. Status: {}", teamName, e.getStatusCode());
            throw new IllegalArgumentException("Team with name '" + teamName + "' not found for future matches.", e);
        } catch (Exception e) {
            log.error("Error fetching future matches for '{}'", teamName, e);
            throw new TeamServiceException("Error fetching future matches.", e);
        }
    }

    /**
     * Builds the URL manually to avoid double encoding.
     */
    private String buildTeamUrl(String teamName) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append("/api/scrape/team?teamName=");

        urlBuilder.append(encodeValue(teamName));

        return urlBuilder.toString();
    }

    /**
     * Builds the URL to get the future matches of a team.
     */
    private String buildFutureMatchesUrl(String teamName) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append("/api/scrape/futureMatches?teamName=");

        urlBuilder.append(encodeValue(teamName));

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