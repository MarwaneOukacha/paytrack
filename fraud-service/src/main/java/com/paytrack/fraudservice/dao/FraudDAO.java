package com.paytrack.fraudservice.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * FraudDAO — requêtes complexes pour la détection de fraude.
 * Séparé du repository pour garder la logique de détection isolée.
 */
@Repository
public class FraudDAO {

    @PersistenceContext
    private EntityManager em;

    /**
     * Compte combien de paiements un compte a effectués dans les X dernières secondes.
     * C'est la requête centrale de la détection de fraude.
     *
     * Appelée par FraudDetector pour chaque message payment.initiated reçu.
     */
    public long countRecentPayments(String accountId, int windowSeconds) {
        String jpql = """
            SELECT COUNT(f) FROM FraudAlert f
            WHERE f.accountId = :accountId
              AND f.detectedAt >= :since
            """;
        LocalDateTime since = LocalDateTime.now().minusSeconds(windowSeconds);
        return (long) em.createQuery(jpql)
            .setParameter("accountId", accountId)
            .setParameter("since", since)
            .getSingleResult();
    }

    /**
     * Comptes les plus à risque sur les dernières 24h.
     * Utilisé par GET /fraud/stats pour le top des comptes suspects.
     * Retourne Object[] : [accountId (String), alertCount (long)]
     */
    public List<Object[]> getTopSuspectAccounts(int limit) {
        String jpql = """
            SELECT f.accountId, COUNT(f)
            FROM FraudAlert f
            WHERE f.detectedAt >= :since
            GROUP BY f.accountId
            ORDER BY COUNT(f) DESC
            """;
        return em.createQuery(jpql, Object[].class)
            .setParameter("since", LocalDateTime.now().minusHours(24))
            .setMaxResults(limit)
            .getResultList();
    }

    /**
     * Distribution des alertes par heure — pour le graphique du dashboard fraude.
     * Retourne Object[] : [heure (int), count (long)]
     */
    public List<Object[]> countAlertsByHourLast24h() {
        String jpql = """
            SELECT FUNCTION('HOUR', f.detectedAt), COUNT(f)
            FROM FraudAlert f
            WHERE f.detectedAt >= :since
            GROUP BY FUNCTION('HOUR', f.detectedAt)
            ORDER BY FUNCTION('HOUR', f.detectedAt)
            """;
        return em.createQuery(jpql, Object[].class)
            .setParameter("since", LocalDateTime.now().minusHours(24))
            .getResultList();
    }
}