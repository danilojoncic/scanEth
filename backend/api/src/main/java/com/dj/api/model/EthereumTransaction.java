//
//import jakarta.persistence.*;
//import java.math.BigInteger;
//import java.time.Instant;
//
//@Entity
//@Table(name = "ethereum_transactions")
//public class EthereumTransaction {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    // Blockchain data
//    @Column(nullable = false)
//    private String hash;
//
//    @Column(nullable = false)
//    private String blockHash;
//
//    @Column(nullable = false)
//    private Long blockNumber;
//
//    @Column(nullable = false)
//    private Long transactionIndex;
//
//    @Column(nullable = false)
//    private Instant timeStamp;
//
//    // Addresses
//    @Column(nullable = false)
//    private String fromAddress;
//
//    @Column(nullable = false)
//    private String toAddress;
//
//    // Amount in wei
//    @Column(nullable = false)
//    private BigInteger value;
//
//    // Gas info
//    @Column(nullable = false)
//    private BigInteger gas;
//
//    @Column(nullable = false)
//    private BigInteger gasPrice;
//
//    @Column(nullable = false)
//    private BigInteger gasUsed;
//
//    @Column(nullable = false)
//    private BigInteger cumulativeGasUsed;
//
//    @Column(nullable = false)
//    private boolean isError;
//
//    @Column(nullable = false)
//    private boolean txReceiptStatus;
//
//    // Optional contract info
//    private String contractAddress;
//    private String methodId;
//    private String functionName;
//
