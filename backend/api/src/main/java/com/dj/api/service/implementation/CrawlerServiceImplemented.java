package com.dj.api.service.implementation;

import com.dj.api.service.CrawlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CrawlerServiceImplemented implements CrawlerService {

    private final WebClient webClient;

    @Value("${etherscan.api.key}")
    private String apiKey;

    @Value("${etherscan.base.url}")
    private String ETHERSCAN_BASE_URL;


    public Mono<String> getTransactionsByAddress(String address, long startBlock, long endBlock) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("api.etherscan.io")
                        .path("/api")
                        .queryParam("module", "account")
                        .queryParam("action", "txlist")
                        .queryParam("address", address)
                        .queryParam("startblock", startBlock)
                        .queryParam("endblock", endBlock)
                        .queryParam("page", 1)
                        .queryParam("offset", 1000)
                        .queryParam("sort", "asc")
                        .queryParam("apikey", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(String.class);
    }


    @Scheduled(fixedRate = 5000)
    public Mono<Long> getLatestBlockNumber() {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("api.etherscan.io")
                        .path("/api")
                        .queryParam("module", "proxy")
                        .queryParam("action", "eth_blockNumber")
                        .queryParam("apikey", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    try {
                        com.fasterxml.jackson.databind.JsonNode root =
                                new com.fasterxml.jackson.databind.ObjectMapper().readTree(response);
                        String hexBlock = root.path("result").asText();
                        return Long.parseLong(hexBlock.substring(2), 16); // convert hex to decimal
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse latest block number", e);
                    }
                });
    }

}
