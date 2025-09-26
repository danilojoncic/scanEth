package com.dj.api.service.implementation;

import com.dj.api.model.EthereumTransaction;
import com.dj.api.repository.EthereumRepository;
import com.dj.api.service.CrawlerService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CrawlerServiceImplemented implements CrawlerService {

    private final WebClient webClient;
    private final EthereumRepository ethereumRepository;

    @Value("${etherscan.api.key}")
    private String apiKey;

    private Long latestBlockNumber = null;


    //https://docs.etherscan.io/get-an-addresss-full-transaction-history sjajni su
    public Page<EthereumTransaction> getTransactionsByAddressPaged(
            String address, long startBlock, int page, int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Page<EthereumTransaction> dbTransactions =
                ethereumRepository.findTransactions(address, startBlock, pageable);


        long lastKnownBlock = dbTransactions.stream()
                .mapToLong(EthereumTransaction::getBlockNumber)
                .max()
                .orElse(startBlock - 1);

        if (latestBlockNumber == null) {
            latestBlockNumber = getLatestBlockNumber();
        }

        if (lastKnownBlock < latestBlockNumber) {
            List<EthereumTransaction> newTxs = fetchTransactionsFromEtherscan(address, lastKnownBlock + 1, latestBlockNumber,page,size);
            if (!newTxs.isEmpty()) {
                ethereumRepository.saveAll(newTxs);
            }
        }

        return ethereumRepository.findTransactions(address, startBlock, pageable);

    }

    private List<EthereumTransaction> fetchTransactionsFromEtherscan(String address, long startBlock, long endBlock,int page,int offset) {
        String response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("api.etherscan.io")
                        .path("/api")
                        .queryParam("module", "account")
                        .queryParam("action", "txlist")
                        .queryParam("address", address)
                        .queryParam("startblock", startBlock)
                        .queryParam("endblock", endBlock)
                        .queryParam("sort", "asc")
                        .queryParam("apikey", apiKey)
                        .queryParam("page", page+1)//because spring is 0 indexed and etherscan is 1 indexed
                        .queryParam("offset", offset)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        List<EthereumTransaction> newTransactions = new ArrayList<>();
        try {
            JsonNode root = new ObjectMapper().readTree(response);
            JsonNode result = root.path("result");

            if (result.isArray()) {
                for (JsonNode txNode : result) {
                    String hash = txNode.path("hash").asText();

                    if (ethereumRepository.existsByHash(hash)) continue;

                    EthereumTransaction tx = new EthereumTransaction();
                    tx.setHash(hash);
                    tx.setBlockHash(txNode.path("blockHash").asText());
                    tx.setBlockNumber(txNode.path("blockNumber").asLong());
                    tx.setTransactionIndex(txNode.path("transactionIndex").asLong());
                    tx.setTimeStamp(Instant.ofEpochSecond(txNode.path("timeStamp").asLong()));
                    tx.setFromAddress(txNode.path("from").asText().toLowerCase());
                    tx.setToAddress(txNode.path("to").asText().toLowerCase());
                    tx.setValue(new BigInteger(txNode.path("value").asText()));
                    tx.setGas(new BigInteger(txNode.path("gas").asText()));
                    tx.setGasPrice(new BigInteger(txNode.path("gasPrice").asText()));
                    tx.setGasUsed(new BigInteger(txNode.path("gasUsed").asText()));
                    tx.setCumulativeGasUsed(new BigInteger(txNode.path("cumulativeGasUsed").asText()));
                    tx.setError("1".equals(txNode.path("isError").asText()));
                    tx.setTxReceiptStatus("1".equals(txNode.path("txreceipt_status").asText()));
                    tx.setContractAddress(txNode.path("contractAddress").asText(null));
                    tx.setMethodId(txNode.path("input").asText(null));
                    tx.setFunctionName(parseFunctionName(tx.getMethodId()));

                    newTransactions.add(tx);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse transactions from Etherscan", e);
        }

        return newTransactions;
    }



    private String parseFunctionName(String input) {
        if (input == null || input.isEmpty() || input.equals("0x")) return null;

        String methodId = input.substring(0, 10);

        switch (methodId) {
            case "0xa9059cbb": return "transfer(address,uint256)";
            case "0x095ea7b3": return "approve(address,uint256)";
            case "0x23b872dd": return "transferFrom(address,address,uint256)";
            default: return methodId; // fallback: just store the method ID
        }
    }

    @Scheduled(fixedRate = 60000)
    private long getLatestBlockNumber() {
        String response = webClient.get()
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
                .block();

        try {
            JsonNode root = new ObjectMapper().readTree(response);
            String hexBlock = root.path("result").asText();
            return Long.parseLong(hexBlock.substring(2), 16);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse latest block number", e);
        }
    }

}
