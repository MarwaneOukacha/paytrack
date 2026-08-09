package com.paytrack.fraudservice.repository;

import com.paytrack.fraudservice.entity.FraudStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FraudStatsRepository extends JpaRepository<FraudStats, String> {

    // Stats d'un compte — GET /fraud/alerts/{accountId}
    Optional<FraudStats> findByAccountId(String accountId);

    // Top comptes les plus flaggés — utile pour le dashboard fraude
    @Query("SELECT f FROM FraudStats f ORDER BY f.totalFlags DESC")
    List<FraudStats> findTopFlagged(org.springframework.data.domain.Pageable pageable);
}