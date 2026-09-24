package com.paytrack.shared.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateSystemSettingRequest {

    /** Clé de la configuration (requise en mise à jour par lot). */
    private String key;

    @NotNull
    private String value;

    private String description;

    private String updatedBy;
}