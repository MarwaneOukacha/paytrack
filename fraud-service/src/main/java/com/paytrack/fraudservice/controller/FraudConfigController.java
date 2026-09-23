package com.paytrack.fraudservice.controller;

import com.paytrack.fraudservice.service.FraudConfigService;
import com.paytrack.shared.dto.CreateFraudConfigRequest;
import com.paytrack.shared.dto.FraudConfigDto;
import com.paytrack.shared.dto.UpdateFraudConfigRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fraud/configs")
@RequiredArgsConstructor
public class FraudConfigController {

    private final FraudConfigService configService;

    @GetMapping
    public ResponseEntity<List<FraudConfigDto>> getConfigs() {
        return ResponseEntity.ok(configService.getConfigs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FraudConfigDto> getConfig(@PathVariable UUID id) {
        return ResponseEntity.ok(configService.getConfigById(id));
    }

    @PostMapping
    public ResponseEntity<FraudConfigDto> createConfig(
            @Valid @RequestBody CreateFraudConfigRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(configService.createConfig(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FraudConfigDto> updateConfig(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateFraudConfigRequest request) {

        return ResponseEntity.ok(configService.updateConfig(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConfig(@PathVariable UUID id) {

        configService.deleteConfig(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/toggle")
    public ResponseEntity<FraudConfigDto> toggleConfig(@PathVariable UUID id) {

        return ResponseEntity.ok(configService.toggleConfig(id));
    }

    @PostMapping("/defaults")
    public ResponseEntity<List<FraudConfigDto>> resetDefaults() {

        return ResponseEntity.ok(configService.resetDefaults());
    }
}