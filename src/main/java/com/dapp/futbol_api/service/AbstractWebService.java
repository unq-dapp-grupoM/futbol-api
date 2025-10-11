package com.dapp.futbol_api.service;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractWebService {

    private static final Logger log = LoggerFactory.getLogger(AbstractWebService.class);
    protected static final String NOT_FOUND = "Not found";

    protected final RestTemplate restTemplate;

    public AbstractWebService(RestTemplateBuilder restTemplateBuilder, String scraperServiceUrl) {
        this.restTemplate = restTemplateBuilder.rootUri(scraperServiceUrl).build();
    }

}