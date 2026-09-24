package com.paytrack.paytrackpaymentservice.service;

import com.paytrack.shared.dto.CardDto;
import com.paytrack.shared.dto.CardNumberDto;
import com.paytrack.shared.dto.CreateCardRequest;
import com.paytrack.shared.dto.UpdateCardRequest;
import com.paytrack.shared.enums.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CardService {

    CardDto createCard(CreateCardRequest request);

    Page<CardDto> getCards(CardStatus status, UUID accountId, Pageable pageable);

    CardDto getCardById(UUID id);

    Page<CardDto> getCardsByAccount(UUID accountId, Pageable pageable);

    CardDto updateCard(UUID id, UpdateCardRequest request);

    CardDto changeStatus(UUID id, CardStatus targetStatus);

    CardNumberDto revealNumber(UUID id);

    void cancelCard(UUID id);
}