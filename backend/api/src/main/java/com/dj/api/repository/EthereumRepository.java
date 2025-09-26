package com.dj.api.repository;

import com.dj.api.model.EthereumTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EthereumRepository extends JpaRepository<EthereumTransaction, Long> {
    @Query("SELECT t FROM EthereumTransaction t " +
            "WHERE (t.fromAddress = :address OR t.toAddress = :address) " +
            "AND t.blockNumber >= :startBlock")
    Page<EthereumTransaction> findTransactions(
            @Param("address") String address,
            @Param("startBlock") Long startBlock,
            Pageable pageable);

    boolean existsByHash(String hash);
}
