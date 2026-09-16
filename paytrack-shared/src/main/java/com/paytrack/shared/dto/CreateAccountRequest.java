package com.paytrack.shared.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateAccountRequest {

    @NotBlank
    private String ownerName;

    @NotBlank
    @Email
    private String ownerEmail;

    @NotBlank
    @Size(min = 3, max = 3)
    private String currency;
}