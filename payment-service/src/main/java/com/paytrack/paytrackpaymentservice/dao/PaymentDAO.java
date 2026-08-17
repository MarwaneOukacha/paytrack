package com.paytrack.paytrackpaymentservice.dao;

import com.paytrack.paytrackpaymentservice.entity.Payment;
import com.paytrack.shared.enums.PaymentStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class PaymentDAO {

    @PersistenceContext
    private EntityManager em;

    public BigDecimal getTotalAmountToday() {

        String jpql =
                "SELECT COALESCE(SUM(p.amount), 0) " +
                        "FROM Payment p " +
                        "WHERE p.status = :status " +
                        "AND p.createdAt >= :start";

        return (BigDecimal) em.createQuery(jpql)
                .setParameter("status", PaymentStatus.PROCESSED)
                .setParameter("start", LocalDate.now().atStartOfDay())
                .getSingleResult();
    }

    public List<Object[]> countByHourLast24h() {

        String jpql =
                "SELECT FUNCTION('HOUR', p.createdAt), COUNT(p) " +
                        "FROM Payment p " +
                        "WHERE p.createdAt >= :since " +
                        "GROUP BY FUNCTION('HOUR', p.createdAt) " +
                        "ORDER BY FUNCTION('HOUR', p.createdAt)";

        return em.createQuery(jpql, Object[].class)
                .setParameter("since", LocalDateTime.now().minusHours(24))
                .getResultList();
    }

    public List<Payment> findWithFilters(
            PaymentStatus status,
            String accountId,
            LocalDateTime from,
            LocalDateTime to,
            int offset,
            int limit
    ) {

        StringBuilder jpql =
                new StringBuilder("SELECT p FROM Payment p WHERE 1=1");

        if (status != null) {
            jpql.append(" AND p.status = :status");
        }

        if (accountId != null) {
            jpql.append(" AND p.accountId = :accountId");
        }

        if (from != null) {
            jpql.append(" AND p.createdAt >= :from");
        }

        if (to != null) {
            jpql.append(" AND p.createdAt <= :to");
        }

        jpql.append(" ORDER BY p.createdAt DESC");

        TypedQuery<Payment> query =
                em.createQuery(jpql.toString(), Payment.class);

        if (status != null) {
            query.setParameter("status", status);
        }

        if (accountId != null) {
            query.setParameter("accountId", accountId);
        }

        if (from != null) {
            query.setParameter("from", from);
        }

        if (to != null) {
            query.setParameter("to", to);
        }

        return query
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public long countWithFilters(
            PaymentStatus status,
            String accountId,
            LocalDateTime from,
            LocalDateTime to
    ) {

        StringBuilder jpql =
                new StringBuilder("SELECT COUNT(p) FROM Payment p WHERE 1=1");

        if (status != null) {
            jpql.append(" AND p.status = :status");
        }

        if (accountId != null) {
            jpql.append(" AND p.accountId = :accountId");
        }

        if (from != null) {
            jpql.append(" AND p.createdAt >= :from");
        }

        if (to != null) {
            jpql.append(" AND p.createdAt <= :to");
        }

        TypedQuery<Long> query =
                em.createQuery(jpql.toString(), Long.class);

        if (status != null) {
            query.setParameter("status", status);
        }

        if (accountId != null) {
            query.setParameter("accountId", accountId);
        }

        if (from != null) {
            query.setParameter("from", from);
        }

        if (to != null) {
            query.setParameter("to", to);
        }

        return query.getSingleResult();
    }
}