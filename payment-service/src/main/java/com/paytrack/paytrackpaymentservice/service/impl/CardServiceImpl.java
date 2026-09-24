package com.paytrack.paytrackpaymentservice.service.impl;

import com.paytrack.paytrackpaymentservice.entity.Account;
import com.paytrack.paytrackpaymentservice.entity.Card;
import com.paytrack.paytrackpaymentservice.entity.LimitConfig;
import com.paytrack.paytrackpaymentservice.mapper.CardMapper;
import com.paytrack.paytrackpaymentservice.repository.AccountRepository;
import com.paytrack.paytrackpaymentservice.repository.CardRepository;
import com.paytrack.paytrackpaymentservice.service.CardService;
import com.paytrack.paytrackpaymentservice.service.LimitConfigService;
import com.paytrack.shared.dto.CardDto;
import com.paytrack.shared.dto.CardNumberDto;
import com.paytrack.shared.dto.CreateCardRequest;
import com.paytrack.shared.dto.UpdateCardRequest;
import com.paytrack.shared.enums.CardNetwork;
import com.paytrack.shared.enums.CardStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private static final List<CardStatus> TERMINAL_STATUSES =
            List.of(CardStatus.CANCELLED, CardStatus.EXPIRED);

    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;
    private final CardMapper cardMapper;
    private final LimitConfigService limitConfigService;

    @Override
    @Transactional
    public CardDto createCard(CreateCardRequest request) {

        Account account = accountRepository.findByIdForUpdate(request.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Account not found with id: " + request.getAccountId()
                ));

        if (request.getType() == null || request.getNetwork() == null) {
            throw new IllegalArgumentException(
                    "Card type and network are required"
            );
        }

        if (cardRepository.existsByAccountIdAndTypeAndStatusIn(
                account.getId(),
                request.getType(),
                List.of(CardStatus.ACTIVE, CardStatus.INACTIVE, CardStatus.BLOCKED))) {
            throw new IllegalStateException(
                    "An active card of this type already exists for this account"
            );
        }

        Card card = new Card();

        LimitConfig config = limitConfigService.getConfigEntity();

        LocalDate expiry = LocalDate.now().plusYears(4);

        card.setCardNumber(generateCardNumber(request.getNetwork()));
        card.setLastFourDigits(card.getCardNumber()
                .substring(card.getCardNumber().length() - 4));
        card.setMaskedNumber(mask(card.getCardNumber(), card.getLastFourDigits()));
        card.setCvv(generateCvv());
        card.setCardholderName(account.getOwnerName());
        card.setAccountId(account.getId());
        card.setAccountNumber(account.getAccountNumber());
        card.setNetwork(request.getNetwork());
        card.setType(request.getType());
        card.setExpiryMonth(expiry.format(DateTimeFormatter.ofPattern("MM")));
        card.setExpiryYear(expiry.format(DateTimeFormatter.ofPattern("yyyy")));
        card.setCurrency(resolveCurrency(
                request.getCurrency(),
                account.getCurrency(),
                config.getDefaultCurrency()
        ));

        card.setSingleTransactionLimit(
                request.getSingleTransactionLimit() != null
                        ? request.getSingleTransactionLimit()
                        : config.getDefaultSingleTransactionLimit()
        );
        card.setDailyLimit(
                request.getDailyLimit() != null
                        ? request.getDailyLimit()
                        : config.getDefaultDailyLimit()
        );
        card.setMonthlyLimit(
                request.getMonthlyLimit() != null
                        ? request.getMonthlyLimit()
                        : config.getDefaultMonthlyLimit()
        );

        validateLimits(card, config);

        return cardMapper.toDto(cardRepository.save(card));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardDto> getCards(CardStatus status, UUID accountId, Pageable pageable) {

        Page<Card> cards;

        if (status != null && accountId != null) {
            cards = cardRepository.findByAccountIdAndStatus(accountId, status, pageable);
        } else if (status != null) {
            cards = cardRepository.findByStatus(status, pageable);
        } else if (accountId != null) {
            cards = cardRepository.findByAccountId(accountId, pageable);
        } else {
            cards = cardRepository.findAll(pageable);
        }

        return cards.map(cardMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public CardDto getCardById(UUID id) {

        return cardMapper.toDto(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardDto> getCardsByAccount(UUID accountId, Pageable pageable) {

        return cardRepository.findByAccountId(accountId, pageable)
                .map(cardMapper::toDto);
    }

    @Override
    @Transactional
    public CardDto updateCard(UUID id, UpdateCardRequest request) {

        Card card = findById(id);

        if (request.getSingleTransactionLimit() != null) {
            card.setSingleTransactionLimit(request.getSingleTransactionLimit());
        }
        if (request.getDailyLimit() != null) {
            card.setDailyLimit(request.getDailyLimit());
        }
        if (request.getMonthlyLimit() != null) {
            card.setMonthlyLimit(request.getMonthlyLimit());
        }

        validateLimits(card, limitConfigService.getConfigEntity());

        return cardMapper.toDto(cardRepository.save(card));
    }

    @Override
    @Transactional
    public CardDto changeStatus(UUID id, CardStatus targetStatus) {

        if (targetStatus == null) {
            throw new IllegalArgumentException("Target status is required");
        }

        Card card = findById(id);

        CardStatus current = card.getStatus();

        if (current == targetStatus) {
            throw new IllegalArgumentException("Card is already " + targetStatus);
        }

        if (TERMINAL_STATUSES.contains(current)) {
            throw new IllegalStateException(
                    "A " + current.name().toLowerCase() + " card cannot change status"
            );
        }

        switch (targetStatus) {
            case ACTIVE -> {
                if (current != CardStatus.INACTIVE && current != CardStatus.BLOCKED) {
                    throw new IllegalStateException(
                            "Only an inactive or blocked card can be activated"
                    );
                }
            }
            case INACTIVE -> {
                if (current != CardStatus.ACTIVE) {
                    throw new IllegalStateException(
                            "Only an active card can be deactivated"
                    );
                }
            }
            case BLOCKED -> {
                if (current != CardStatus.ACTIVE && current != CardStatus.INACTIVE) {
                    throw new IllegalStateException(
                            "Only active or inactive cards can be blocked"
                    );
                }
            }
            case LOST, STOLEN -> {
                if (current != CardStatus.ACTIVE && current != CardStatus.INACTIVE) {
                    throw new IllegalStateException(
                            "Only active or inactive cards can be declared "
                                    + targetStatus.name().toLowerCase()
                    );
                }
            }
            case EXPIRED -> {
                if (current == CardStatus.LOST || current == CardStatus.STOLEN) {
                    throw new IllegalStateException(
                            "A lost or stolen card cannot be expired"
                    );
                }
            }
            case CANCELLED -> {
                throw new IllegalStateException(
                        "Cancellation must use the dedicated cancellation endpoint"
                );
            }
        }

        card.setStatus(targetStatus);

        return cardMapper.toDto(cardRepository.save(card));
    }

    @Override
    @Transactional(readOnly = true)
    public CardNumberDto revealNumber(UUID id) {

        Card card = findById(id);

        if (card.getStatus() == CardStatus.CANCELLED) {
            throw new IllegalStateException(
                    "A cancelled card cannot be revealed"
            );
        }

        CardNumberDto dto = new CardNumberDto();
        dto.setCardId(card.getId());
        dto.setCardNumber(card.getCardNumber());
        dto.setCvv(card.getCvv());
        dto.setExpiryMonth(card.getExpiryMonth());
        dto.setExpiryYear(card.getExpiryYear());
        dto.setNetwork(card.getNetwork());
        dto.setType(card.getType());

        return dto;
    }

    @Override
    @Transactional
    public void cancelCard(UUID id) {

        Card card = findById(id);

        if (card.getStatus() == CardStatus.CANCELLED) {
            throw new IllegalArgumentException("Card is already cancelled");
        }

        card.setStatus(CardStatus.CANCELLED);

        cardRepository.save(card);
    }

    private Card findById(UUID id) {

        return cardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Card not found with id: " + id
                ));
    }

    private void validateLimits(Card card, LimitConfig config) {

        if (isGreaterThan(card.getSingleTransactionLimit(), config.getMaxSingleTransactionLimit())) {
            throw new IllegalArgumentException(
                    "Single transaction limit exceeds maximum of "
                            + config.getMaxSingleTransactionLimit()
            );
        }
        if (isGreaterThan(card.getDailyLimit(), config.getMaxDailyLimit())) {
            throw new IllegalArgumentException(
                    "Daily limit exceeds maximum of " + config.getMaxDailyLimit()
            );
        }
        if (isGreaterThan(card.getMonthlyLimit(), config.getMaxMonthlyLimit())) {
            throw new IllegalArgumentException(
                    "Monthly limit exceeds maximum of " + config.getMaxMonthlyLimit()
            );
        }
        if (card.getSingleTransactionLimit() != null && card.getDailyLimit() != null
                && card.getSingleTransactionLimit().compareTo(card.getDailyLimit()) > 0) {
            throw new IllegalArgumentException(
                    "Single transaction limit cannot exceed the daily limit"
            );
        }
        if (card.getDailyLimit() != null && card.getMonthlyLimit() != null
                && card.getDailyLimit().compareTo(card.getMonthlyLimit()) > 0) {
            throw new IllegalArgumentException(
                    "Daily limit cannot exceed the monthly limit"
            );
        }
    }

    private boolean isGreaterThan(BigDecimal value, BigDecimal max) {

        return value != null && value.compareTo(max) > 0;
    }

    private String resolveCurrency(String requested, String accountCurrency, String defaultCurrency) {

        if (requested != null && !requested.isBlank()) {
            return requested.trim().toUpperCase();
        }
        if (accountCurrency != null && !accountCurrency.isBlank()) {
            return accountCurrency;
        }
        return defaultCurrency;
    }

    private String generateCardNumber(CardNetwork network) {

        if (network == null) {
            throw new IllegalArgumentException("Card network is required");
        }

        SecureRandom random = new SecureRandom();

        String prefix = network == CardNetwork.VISA ? "4" : "5";

        StringBuilder pan = new StringBuilder(prefix);
        while (pan.length() < 15) {
            pan.append(random.nextInt(10));
        }
        pan.append(luhnCheckDigit(pan.toString()));

        String candidate = pan.toString();

        while (cardRepository.existsByCardNumber(candidate)) {
            StringBuilder retry = new StringBuilder(prefix);
            while (retry.length() < 15) {
                retry.append(random.nextInt(10));
            }
            retry.append(luhnCheckDigit(retry.toString()));
            candidate = retry.toString();
        }

        return candidate;
    }

    private int luhnCheckDigit(String partial) {

        int sum = 0;
        boolean doubleDigit = true;

        for (int i = partial.length() - 1; i >= 0; i--) {
            int d = partial.charAt(i) - '0';
            if (doubleDigit) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            doubleDigit = !doubleDigit;
        }

        return (10 - (sum % 10)) % 10;
    }

    private String generateCvv() {

        SecureRandom random = new SecureRandom();
        return String.format("%03d", random.nextInt(1000));
    }

    private String mask(String pan, String lastFourDigits) {

        return pan.substring(0, 4)
                + " " + pan.substring(4, 6)
                + "** **** " + lastFourDigits;
    }
}