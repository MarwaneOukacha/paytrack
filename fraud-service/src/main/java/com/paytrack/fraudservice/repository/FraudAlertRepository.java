package com.paytrack.fraudservice.repository;

import com.paytrack.fraudservice.entity.FraudAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface FraudAlertRepository extends JpaRepository<FraudAlert, UUID> {

    // Toutes les alertes d'un compte — GET /fraud/alerts/{accountId}
    List<FraudAlert> findByAccountIdOrderByDetectedAtDesc(String accountId);

    // Liste paginée — GET /fraud/alerts
    Page<FraudAlert> findAllByOrderByDetectedAtDesc(Pageable pageable);

    // Alertes dans une fenêtre de temps — pour éviter les doublons
    @Query("""
        SELECT f FROM FraudAlert f
        WHERE f.accountId = :accountId
          AND f.detectedAt >= :since
        """)
    List<FraudAlert> findRecentByAccount(
        @Param("accountId") String accountId,
        @Param("since")     LocalDateTime since
    );

    // Compte d'alertes du jour — utilisé par GET /fraud/stats
    @Query("SELECT COUNT(f) FROM FraudAlert f WHERE f.detectedAt >= :since")
    long countSince(@Param("since") LocalDateTime since);
}