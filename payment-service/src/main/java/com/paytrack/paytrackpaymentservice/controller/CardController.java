package com.paytrack.paytrackpaymentservice.controller;

import com.paytrack.paytrackpaymentservice.service.CardService;
import com.paytrack.shared.dto.CardDto;
import com.paytrack.shared.dto.CardNumberDto;
import com.paytrack.shared.dto.CreateCardRequest;
import com.paytrack.shared.dto.UpdateCardRequest;
import com.paytrack.shared.enums.CardStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@Tag(name = "Cards", description = "Gestion des cartes bancaires liées à un compte PayTrack")
public class CardController {

    private final CardService cardService;

    @PostMapping
    @Operation(summary = "Émettre une carte", description = "Crée une carte virtuelle ou physique rattachée à un compte.")
    public ResponseEntity<CardDto> createCard(
            @Valid @RequestBody CreateCardRequest request) {

        CardDto card = cardService.createCard(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(card);
    }

    @GetMapping
    @Operation(summary = "Lister les cartes", description = "Retourne les cartes, filtrables par statut et/ou compte.")
    public ResponseEntity<Page<CardDto>> getCards(
            @RequestParam(required = false) CardStatus status,
            @RequestParam(required = false) UUID accountId,
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable) {

        Page<CardDto> cards = cardService.getCards(status, accountId, pageable);

        return ResponseEntity.ok(cards);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'une carte", description = "Retourne une carte par son identifiant (numéro masqué).")
    public ResponseEntity<CardDto> getCardById(@PathVariable UUID id) {

        return ResponseEntity.ok(cardService.getCardById(id));
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Cartes d'un compte", description = "Retourne toutes les cartes rattachées à un compte donné.")
    public ResponseEntity<Page<CardDto>> getCardsByAccount(
            @PathVariable UUID accountId,
            @PageableDefault(
                    size = 50,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable) {

        return ResponseEntity.ok(
                cardService.getCardsByAccount(accountId, pageable)
        );
    }

    @GetMapping("/{id}/number")
    @Operation(summary = "Révéler le numéro complet", description = "Permet d'afficher le PAN complet et le CVV d'une carte (usage bureau / carte virtuelle).")
    public ResponseEntity<CardNumberDto> revealNumber(@PathVariable UUID id) {

        return ResponseEntity.ok(cardService.revealNumber(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour les plafonds", description = "Modifie les plafonds de transaction d'une carte.")
    public ResponseEntity<CardDto> updateCard(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCardRequest request) {

        return ResponseEntity.ok(cardService.updateCard(id, request));
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activer une carte")
    public ResponseEntity<CardDto> activateCard(@PathVariable UUID id) {

        return ResponseEntity.ok(
                cardService.changeStatus(id, CardStatus.ACTIVE)
        );
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Désactiver une carte")
    public ResponseEntity<CardDto> deactivateCard(@PathVariable UUID id) {

        return ResponseEntity.ok(
                cardService.changeStatus(id, CardStatus.INACTIVE)
        );
    }

    @PostMapping("/{id}/block")
    @Operation(summary = "Bloquer une carte")
    public ResponseEntity<CardDto> blockCard(@PathVariable UUID id) {

        return ResponseEntity.ok(
                cardService.changeStatus(id, CardStatus.BLOCKED)
        );
    }

    @PostMapping("/{id}/unblock")
    @Operation(summary = "Débloquer une carte")
    public ResponseEntity<CardDto> unblockCard(@PathVariable UUID id) {

        return ResponseEntity.ok(
                cardService.changeStatus(id, CardStatus.ACTIVE)
        );
    }

    @PostMapping("/{id}/expire")
    @Operation(summary = "Marquer une carte comme expirée")
    public ResponseEntity<CardDto> expireCard(@PathVariable UUID id) {

        return ResponseEntity.ok(
                cardService.changeStatus(id, CardStatus.EXPIRED)
        );
    }

    @PostMapping("/{id}/lost")
    @Operation(summary = "Déclarer une carte perdue")
    public ResponseEntity<CardDto> markCardLost(@PathVariable UUID id) {

        return ResponseEntity.ok(
                cardService.changeStatus(id, CardStatus.LOST)
        );
    }

    @PostMapping("/{id}/stolen")
    @Operation(summary = "Déclarer une carte volée")
    public ResponseEntity<CardDto> markCardStolen(@PathVariable UUID id) {

        return ResponseEntity.ok(
                cardService.changeStatus(id, CardStatus.STOLEN)
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Annuler définitivement une carte")
    public ResponseEntity<Void> cancelCard(@PathVariable UUID id) {

        cardService.cancelCard(id);

        return ResponseEntity.noContent().build();
    }
}