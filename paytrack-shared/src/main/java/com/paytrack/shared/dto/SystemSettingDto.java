package com.paytrack.shared.dto;

import com.paytrack.shared.enums.ConfigCategory;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SystemSettingDto {

    private UUID id;

    private String key;

    private String value;

    private String description;

    private ConfigCategory category;

    private String updatedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}