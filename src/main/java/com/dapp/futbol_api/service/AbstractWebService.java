package com.dapp.futbol_api.service;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.DefaultUriBuilderFactory;

public abstract class AbstractWebService {

    private static final Logger log = LoggerFactory.getLogger(AbstractWebService.class);
    protected static final String NOT_FOUND = "Not found";

    protected final RestTemplate restTemplate;

    protected AbstractWebService(RestTemplateBuilder restTemplateBuilder, String scraperServiceUrl) {
        log.info("Initializing WebService with base URL: {}", scraperServiceUrl);

        // Configure RestTemplate to NOT encode URLs automatically
        DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory(scraperServiceUrl);
        uriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);

        this.restTemplate = restTemplateBuilder
                .uriTemplateHandler(uriBuilderFactory)
                .build();
    }
}