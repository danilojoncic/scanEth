package com.dj.api.service.implementation;

import com.dj.api.service.CrawlerService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CrawlerServiceImplemented implements CrawlerService {

    private final WebClient webClient;

    @Value("${etherscan.api.key}")
    private String apiKey;

    private Long latestBlockNumber;

    public Mono<Long> getBlockByTimestamp(long timestamp) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("api.etherscan.io")
                        .path("/api")
                        .queryParam("module", "block")
                        .queryParam("action", "getblocknobytime")
                        .queryParam("timestamp", timestamp)
                        .queryParam("closest", "before")
                        .queryParam("apikey", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    try {
                        com.fasterxml.jackson.databind.JsonNode root =
                                new com.fasterxml.jackson.databind.ObjectMapper().readTree(response);
                        String blockNumberStr = root.path("result").asText();
                        return Long.parseLong(blockNumberStr);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse block number by timestamp", e);
                    }
                });
    }

    //https://docs.etherscan.io/api-endpoints/accounts
    public Mono<String> getBalanceAtBlock(String address, long blockNumber) {
        String hexBlock = "0x" + Long.toHexString(blockNumber);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("api.etherscan.io")
                        .path("/api")
                        .queryParam("module", "account")
                        .queryParam("action", "balance")
                        .queryParam("address", address)
                        .queryParam("tag", hexBlock)
                        .queryParam("apikey", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(String.class);
    }


    //https://docs.etherscan.io/get-an-addresss-full-transaction-history sjajni su
    public Mono<List<JsonNode>> getTransactionsByAddressPaged(String address, long startBlock, int page, int offset) {
        if (latestBlockNumber == null){
            latestBlockNumber = getLatestBlockNumber().block();
        }
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("api.etherscan.io")
                        .path("/api")
                        .queryParam("module", "account")
                        .queryParam("action", "txlist")
                        .queryParam("address", address)
                        .queryParam("startblock", startBlock)
                        .queryParam("endblock", latestBlockNumber)
                        .queryParam("page", page)
                        .queryParam("offset", offset)
                        .queryParam("sort", "asc")
                        .queryParam("apikey", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    try {
                        JsonNode root = new ObjectMapper().readTree(response);
                        JsonNode result = root.path("result");
                        List<JsonNode> txs = new ArrayList<>();
                        if (result.isArray()) {
                            result.forEach(txs::add);
                        }
                        return txs;
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse transactions", e);
                    }
                });
    }


    public Mono<String> getBalanceAtTime(String address, long timestamp) {
        return getBlockByTimestamp(timestamp)
                .flatMap(blockNumber -> getBalanceAtBlock(address, blockNumber));
    }




    @Scheduled(fixedRate = 12000)
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
                        return Long.parseLong(hexBlock.substring(2), 16);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse latest block number", e);
                    }
                });
    }

}
