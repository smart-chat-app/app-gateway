package com.smartchat.gateway.provisioning;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Representation of the user payload sent to Keycloak admin API.
 */
public record KeycloakUserRepresentation(
        String id,
        String username,
        String firstName,
        String lastName,
        String email,
        boolean emailVerified,
        boolean enabled,
        Map<String, List<String>> attributes,
        List<CredentialRepresentation> credentials) {

    static KeycloakUserRepresentation from(CreateUserRequest request) {
        // Derive some defaults if lastName/email are not provided
        String lastName = request.lastName() != null ? request.lastName() : "User";
        String email = request.email() != null ? request.email() : request.username() + "@example.com";

        return new KeycloakUserRepresentation(
                null, // let Keycloak generate the id
                request.username(),
                request.displayName(),  // firstName
                lastName,
                email,
                true,   // emailVerified
                true,   // enabled
                attributesFrom(request),
                credentialsFrom(request));
    }

    private static Map<String, List<String>> attributesFrom(CreateUserRequest request) {
        Map<String, List<String>> attributes = new LinkedHashMap<>();
        addAttribute(attributes, "displayName", request.displayName());
        addAttribute(attributes, "bio", request.bio());
        addAttribute(attributes, "avatarUrl", request.avatarUrl());
        return attributes;
    }

    private static void addAttribute(Map<String, List<String>> attributes, String key, String value) {
        if (value != null && !value.isBlank()) {
            attributes.put(key, List.of(value));
        }
    }

    private static List<CredentialRepresentation> credentialsFrom(CreateUserRequest request) {
        // For now derive an initial password from the username; adjust to real password flow later
        String password = hashedPassword(request.username());
        return List.of(new CredentialRepresentation("password", false, password));
    }

    private static String hashedPassword(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Missing SHA-256 MessageDigest", e);
        }
    }
}
