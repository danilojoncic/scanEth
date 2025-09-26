package com.dj.api.controller;

import com.dj.api.model.EthereumTransaction;
import com.dj.api.service.implementation.CrawlerServiceImplemented;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/scan")
@RequiredArgsConstructor
@CrossOrigin("*")
public class TaskController {

    private final CrawlerServiceImplemented crawlerServiceImplemented;

    /**
     * Endpoint that returns all transactions from an address starting from a block number, its paginated to
     * prevent the overuse of resources
     * @param address The address that the requested transactions have as the sender or the reciepient
     * @param startBlock Block number from which we will start checking the transactions up until the latest block number
     * @param page self explanatory
     * @param size self explanatory
     * @return A ResponseEntity which includes a list of all transactions which include the address and begin from one block
     */
    @GetMapping("/transactions")
    public ResponseEntity<?> getTransactions(
            @RequestParam String address,
            @RequestParam Long startBlock,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "50") int size
    ) {

        /// Ethereum address format check
        if (address == null || !address.matches("^0x[a-fA-F0-9]{40}$")) {
            return ResponseEntity.badRequest().body("Invalid Ethereum address");
        }

        /// block number check
        if (startBlock == null || startBlock < 0) {
            return ResponseEntity.badRequest().body("startBlock must be a non-negative number");
        }

        if (page < 0 || size <= 0) {
            return ResponseEntity.badRequest().body("page must be >= 0 and size must be > 0");
        }



        try {
            Page<EthereumTransaction> transactions = crawlerServiceImplemented
                    .getTransactionsByAddressPaged(address, startBlock, page, size);
            return ResponseEntity.ok(transactions.getContent());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to fetch transactions: " + e.getMessage());
        }
    }


    /***
     * Endpoint that returns the balance in ETH of an address at a certain date in time
     * @param address The address we look at
     * @param date The date in question, it must be in the following format YYYY-MM-DD, and cannot be in the future
     * @return A ResponseEntity that includes the balance in ETH, A Big Decimal instance
     */
    @GetMapping("/balance")
    public ResponseEntity<?> getBalanceAtDate(
            @RequestParam String address,
            @RequestParam String date // YYYY-MM-DD
    ) {

        if (address == null || !address.matches("^0x[a-fA-F0-9]{40}$")) {
            return ResponseEntity.badRequest().body("Invalid Ethereum address");
        }

        /// Date format check
        LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("Invalid date format. Use YYYY-MM-DD");
        }

        /// Future check
        if (parsedDate.isAfter(LocalDate.now())) {
            return ResponseEntity.badRequest().body("Date cannot be in the future");
        }



        try {
            BigInteger balanceWei = crawlerServiceImplemented.getBalanceAtDate(address, date);
            BigDecimal balanceEth = new BigDecimal(balanceWei)
                    .divide(BigDecimal.TEN.pow(18));
            return ResponseEntity.ok(balanceEth.toPlainString() + " ETH");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to fetch balance: " + e.getMessage());
        }
    }

}
