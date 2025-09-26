package com.dj.api.model;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigInteger;
import java.time.Instant;

/***
 * POJO Class that represent the Ethereum Transaction, it is used for the Postgres database
 */
@Entity
@Table(name = "ethereum_transactions")
@Data
public class EthereumTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /// Duplicates are negated by checking via the hash, as the hash is pseudo-random
    @Column(nullable = false,unique = true)
    private String hash;

    @Column(nullable = false)
    private String blockHash;

    @Column(nullable = false)
    private Long blockNumber;

    @Column(nullable = false)
    private Long transactionIndex;

    @Column(nullable = false)
    private Instant timeStamp;

    // Addresses
    @Column(nullable = false)
    private String fromAddress;

    @Column(nullable = false)
    private String toAddress;

    // Amount in wei
    @Column(nullable = false)
    private BigInteger value;

    // Gas info
    @Column(nullable = false)
    private BigInteger gas;

    @Column(nullable = false)
    private BigInteger gasPrice;

    @Column(nullable = false)
    private BigInteger gasUsed;

    @Column(nullable = false)
    private BigInteger cumulativeGasUsed;

    @Column(nullable = false)
    private boolean isError;

    @Column(nullable = false)
    private boolean txReceiptStatus;

    private String contractAddress;
    private String methodId;
    private String functionName;
}

