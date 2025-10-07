package com.dapp.futbol_api.config;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class PlaywrightConfig {

    @Bean(destroyMethod = "close")
    public Playwright playwright() {
        return Playwright.create();
    }

    @Bean(destroyMethod = "close")
    public Browser browser(Playwright playwright) {
        return playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(true)
                .setArgs(List.of(
                        "--no-sandbox",
                        "--disable-setuid-sandbox",
                        "--disable-dev-shm-usage", // Muy importante para Render/Docker
                        "--disable-blink-features=AutomationControlled"
                )));
    }
}
