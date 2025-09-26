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
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CrawlerServiceImplemented implements CrawlerService {

    private final WebClient webClient;
    private final EthereumRepository ethereumRepository;
    private final Web3j web3j;

    @Value("${etherscan.api.key}")
    private String apiKey;

    private Long latestBlockNumber = null;


    /***
     * Gets a page of transactions from an etheruem address, if the transactions are
     * not in the database they are fetched using the Etherscan API and then delivered
     * @param address self explanatory
     * @param startBlock Begining of the range of blocks that we will look at
     *                   the end is always the current pseudo-latest block
     * @param page self explanatory
     * @param size self explanatory
     * @return Page of ethereum transactions
     */
    //https://docs.etherscan.io/get-an-addresss-full-transaction-history sjajni su
    @Override
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

    /***
     * Private helper functuon that fetchets the transactions from the Etherscan API
     * @param address self explanatory
     * @param startBlock self explanatory
     * @param endBlock self explanatory
     * @param page self explanatory
     * @param offset self explanatory
     * @return A List of Ethereum Transactions
     */
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


    /***
     * Parses the functionName field and from it gets the different types used from the lookup map
     * @param input self explanatory
     * @return Fully parsed function name
     */
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

    /***
     * Gives us the ETH Balance of an address at a specified date in time, uses the Alchemy API for the balance
     * and the Etherscan API for getting the block number from the date, first we convert the date to UTC
     * from it get a  timestamp, send the timestamp into Etherscan and then use the blocknumber for getting
     * the final balance number at that time with Alchemy
     * @param address self explanatory
     * @param date self explanatory
     * @return BigInteger representing the ETH Balance, as WEI, it later on needs to be converted into
     * a decimal value for the true ETH Balance, using Big Decimal instances
     */
    @Override
    public BigInteger getBalanceAtDate(String address, String date) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            Instant targetInstant = localDate.atStartOfDay(ZoneOffset.UTC).toInstant();
            long timestamp = targetInstant.getEpochSecond();

            String blockResponse = webClient.get()
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
                    .block();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode blockRoot = mapper.readTree(blockResponse);
            long blockNumber = blockRoot.path("result").asLong();

            return web3j.ethGetBalance(address, new DefaultBlockParameterNumber(blockNumber))
                    .send()
                    .getBalance();

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch balance at date " + date, e);
        }
    }


    /***
     * Scheduled method that is ran every 60 seconds to fetch the latest block number
     * using the Etherscan API, 60 seconds is a nice round number, i did not want to
     * spend all of my API limits quickly
     * @return A long representing the pseudo-latest block number
     */
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
