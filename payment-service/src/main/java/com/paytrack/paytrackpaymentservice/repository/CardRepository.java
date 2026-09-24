package com.paytrack.paytrackpaymentservice.repository;

import com.paytrack.paytrackpaymentservice.entity.Card;
import com.paytrack.shared.enums.CardStatus;
import com.paytrack.shared.enums.CardType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {

    Optional<Card> findByCardNumber(String cardNumber);

    boolean existsByCardNumber(String cardNumber);

    Page<Card> findByAccountId(UUID accountId, Pageable pageable);

    Page<Card> findByStatus(CardStatus status, Pageable pageable);

    Page<Card> findByAccountIdAndStatus(UUID accountId, CardStatus status, Pageable pageable);

    boolean existsByAccountIdAndTypeAndStatusIn(
            UUID accountId,
            CardType type,
            Collection<CardStatus> statuses);
}