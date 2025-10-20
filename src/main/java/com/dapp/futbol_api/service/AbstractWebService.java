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

    public AbstractWebService(RestTemplateBuilder restTemplateBuilder, String scraperServiceUrl) {
        log.info("Initializing WebService with base URL: {}", scraperServiceUrl);

        // Configurar RestTemplate para NO codificar automáticamente las URLs
        DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory(scraperServiceUrl);
        uriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);

        this.restTemplate = restTemplateBuilder
                .uriTemplateHandler(uriBuilderFactory)
                .build();
    }
}