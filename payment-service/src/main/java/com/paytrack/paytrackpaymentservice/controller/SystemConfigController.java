package com.paytrack.paytrackpaymentservice.controller;

import com.paytrack.paytrackpaymentservice.service.SystemConfigService;
import com.paytrack.shared.dto.SystemSettingDto;
import com.paytrack.shared.dto.UpdateSystemSettingRequest;
import com.paytrack.shared.enums.ConfigCategory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/configs/system")
@RequiredArgsConstructor
@Tag(name = "System Config", description = "Configuration générale du système PayTrack (paramètres, intégrations, journaux)")
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @GetMapping
    @Operation(summary = "Lister les configurations système",
            description = "Retourne toutes les configurations, éventuellement filtrées par catégorie.")
    public ResponseEntity<List<SystemSettingDto>> getSettings(
            @RequestParam(required = false) ConfigCategory category) {

        return ResponseEntity.ok(systemConfigService.getSettings(category));
    }

    @GetMapping("/{key}")
    @Operation(summary = "Lire une configuration système")
    public ResponseEntity<SystemSettingDto> getSetting(@PathVariable String key) {

        return ResponseEntity.ok(systemConfigService.getSetting(key));
    }

    @PutMapping
    @Operation(summary = "Mettre à jour plusieurs configurations",
            description = "Met à jour, en une seule requête, la valeur de plusieurs configurations existantes.")
    public ResponseEntity<List<SystemSettingDto>> updateSettings(
            @Valid @RequestBody List<UpdateSystemSettingRequest> requests) {

        return ResponseEntity.ok(systemConfigService.updateSettings(requests));
    }

    @PutMapping("/{key}")
    @Operation(summary = "Mettre à jour une configuration système")
    public ResponseEntity<SystemSettingDto> updateSetting(
            @PathVariable String key,
            @Valid @RequestBody UpdateSystemSettingRequest request) {

        return ResponseEntity.ok(
                systemConfigService.updateSetting(key, request)
        );
    }

    @PostMapping("/defaults")
    @Operation(summary = "Restaurer les valeurs par défaut",
            description = "Réinitialise l'ensemble des configurations système à leurs valeurs d'origine.")
    public ResponseEntity<List<SystemSettingDto>> resetDefaults() {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(systemConfigService.resetDefaults());
    }
}