package com.dapp.futbol_api.security;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @Schema(description = "User's email address.",
            example = "user@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(description = "User's password. Must be at least 6 characters long and contain at least one number.",
            example = "password123",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
