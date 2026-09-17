package com.paytrack.shared.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateAccountRequest {

    @Size(max = 100, message = "Owner name cannot exceed 100 characters")
    private String ownerName;

    @Email(message = "Invalid email format")
    @Size(max = 150, message = "Owner email cannot exceed 150 characters")
    private String ownerEmail;

    @Size(min = 3, max = 3, message = "Currency must be exactly 3 letters")
    private String currency;
}