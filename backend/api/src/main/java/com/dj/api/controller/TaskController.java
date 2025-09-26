package com.dj.api.controller;

import com.dj.api.service.implementation.CrawlerServiceImplemented;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping("/api/scan")
@RequiredArgsConstructor
public class TaskController {

    private final CrawlerServiceImplemented crawlerServiceImplemented;

    @GetMapping("/transactions")
    public ResponseEntity<?> getTransactions(
            @RequestParam String address,
            @RequestParam Long startBlock,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "100") int size
    ) {
        try {

            List<JsonNode> transactions = crawlerServiceImplemented
                    .getTransactionsByAddressPaged(address, startBlock, page, size)
                    .block();
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to fetch transactions: " + e.getMessage());
        }
    }
}
