package com.smartchat.gateway.provisioning;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @NotBlank String username,
        @Email String email,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String password) {
}
