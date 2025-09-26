package com.dj.api.service;


import com.dj.api.model.EthereumTransaction;
import org.springframework.data.domain.Page;

import java.math.BigInteger;

public interface CrawlerService {
    BigInteger getBalanceAtDate(String address, String date);
    Page<EthereumTransaction> getTransactionsByAddressPaged(
            String address, long startBlock, int page, int size
    );
}


