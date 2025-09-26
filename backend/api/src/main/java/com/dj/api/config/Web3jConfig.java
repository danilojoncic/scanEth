package com.dj.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@Configuration
public class Web3jConfig {

    @Bean
    public Web3j web3j(
            @Value("${web3j.base.url}") String baseUrl,
            @Value("${alchemy.api.key}") String apiKey) {

        String fullUrl = baseUrl + "/" + apiKey;
        return Web3j.build(new HttpService(fullUrl));
    }
}

