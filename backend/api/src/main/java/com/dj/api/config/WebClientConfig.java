package com.dj.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${etherscan.base.url}")
    private String baseUrl;

    @Bean
    public WebClient etherscanWebClient(WebClient.Builder builder) {
        return builder.baseUrl(baseUrl).build();
    }
}
