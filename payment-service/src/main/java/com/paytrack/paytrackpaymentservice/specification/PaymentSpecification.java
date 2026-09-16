package com.paytrack.paytrackpaymentservice.specification;

import com.paytrack.paytrackpaymentservice.entity.Payment;
import com.paytrack.shared.enums.PaymentStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentSpecification {

    public static Specification<Payment> hasAccountId(String accountId) {
        return (root, query, cb) ->
                accountId == null
                        ? null
                        : cb.equal(root.get("accountId"), accountId);
    }

    public static Specification<Payment> hasToAccountNumber(String toAccountNumber) {
        return (root, query, cb) ->
                toAccountNumber == null
                        ? null
                        : cb.equal(root.get("toAccountNumber"), toAccountNumber);
    }

    public static Specification<Payment> hasStatus(PaymentStatus status) {
        return (root, query, cb) ->
                status == null
                        ? null
                        : cb.equal(root.get("status"), status);
    }

    public static Specification<Payment> amountGreaterThanOrEqualTo(BigDecimal minAmount) {
        return (root, query, cb) ->
                minAmount == null
                        ? null
                        : cb.greaterThanOrEqualTo(root.get("amount"), minAmount);
    }

    public static Specification<Payment> amountLessThanOrEqualTo(BigDecimal maxAmount) {
        return (root, query, cb) ->
                maxAmount == null
                        ? null
                        : cb.lessThanOrEqualTo(root.get("amount"), maxAmount);
    }

    public static Specification<Payment> createdAfter(LocalDateTime fromDate) {
        return (root, query, cb) ->
                fromDate == null
                        ? null
                        : cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate);
    }

    public static Specification<Payment> createdBefore(LocalDateTime toDate) {
        return (root, query, cb) ->
                toDate == null
                        ? null
                        : cb.lessThanOrEqualTo(root.get("createdAt"), toDate);
    }
}