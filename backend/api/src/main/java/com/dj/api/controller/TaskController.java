package com.dj.api.controller;

import com.dj.api.model.EthereumTransaction;
import com.dj.api.service.implementation.CrawlerServiceImplemented;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.BigInteger;

@RestController
@RequestMapping("/api/scan")
@RequiredArgsConstructor
@CrossOrigin("*")
public class TaskController {

    private final CrawlerServiceImplemented crawlerServiceImplemented;

    @GetMapping("/transactions")
    public ResponseEntity<?> getTransactions(
            @RequestParam String address,
            @RequestParam Long startBlock,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "50") int size
    ) {
        try {
            Page<EthereumTransaction> transactions = crawlerServiceImplemented
                    .getTransactionsByAddressPaged(address, startBlock, page, size);
            return ResponseEntity.ok(transactions.getContent());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to fetch transactions: " + e.getMessage());
        }
    }


    @GetMapping("/balance")
    public ResponseEntity<?> getBalanceAtDate(
            @RequestParam String address,
            @RequestParam String date // YYYY-MM-DD
    ) {
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
