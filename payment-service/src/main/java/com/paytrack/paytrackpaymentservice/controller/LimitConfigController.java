package com.paytrack.paytrackpaymentservice.controller;

import com.paytrack.paytrackpaymentservice.service.LimitConfigService;
import com.paytrack.shared.dto.LimitConfigDto;
import com.paytrack.shared.dto.UpdateLimitConfigRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/configs/limits")
@RequiredArgsConstructor
@Tag(name = "Limit Config", description = "Plafonds & configs par défaut appliqués à la création des comptes et des cartes")
public class LimitConfigController {

    private final LimitConfigService limitConfigService;

    @GetMapping
    @Operation(summary = "Lire la configuration des plafonds",
            description = "Retourne les plafonds par défaut et les plafonds maximum pour les cartes.")
    public ResponseEntity<LimitConfigDto> getConfig() {

        return ResponseEntity.ok(limitConfigService.getConfig());
    }

    @PutMapping
    @Operation(summary = "Mettre à jour la configuration des plafonds",
            description = "Modifie les plafonds par défaut (appliqués aux nouvelles cartes) et les plafonds maximum autorisés.")
    public ResponseEntity<LimitConfigDto> updateConfig(
            @Valid @RequestBody UpdateLimitConfigRequest request) {

        return ResponseEntity.ok(limitConfigService.updateConfig(request));
    }
}