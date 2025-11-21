package com.smartchat.gateway.provisioning;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @NotBlank String username,
        @NotBlank String displayName,
        String lastName,
        @Email String email,
        String bio,
        String avatarUrl) {
}

