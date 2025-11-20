package com.smartchat.gateway.provisioning;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import com.smartchat.gateway.configuration.KeycloakAdminProperties;

import reactor.core.publisher.Mono;

@Component
public class KeycloakUserClient {

    private final KeycloakAdminProperties properties;
    private final WebClient webClient;

    public KeycloakUserClient(KeycloakAdminProperties properties, WebClient.Builder builder) {
        this.properties = properties;
        this.webClient = builder.baseUrl(properties.baseUrl()).build();
    }

    public Mono<Void> createUser(CreateUserRequest request) {
        return adminToken()
                .flatMap(token -> webClient.post()
                        .uri(properties.usersPath())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(KeycloakUserRepresentation.from(request))
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, ClientResponse::createException)
                        .bodyToMono(Void.class));
    }

    private Mono<String> adminToken() {
        return webClient.post()
                .uri(properties.tokenPath())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "password")
                        .with("client_id", properties.clientId())
                        .with("username", properties.username())
                        .with("password", properties.password()))
                .retrieve()
                .onStatus(HttpStatusCode::isError, ClientResponse::createException)
                .bodyToMono(TokenResponse.class)
                .map(TokenResponse::accessToken);
    }

    private record TokenResponse(String access_token) {
        String accessToken() {
            return access_token;
        }
    }

    private record KeycloakUserRepresentation(String id,
                                              String username,
                                              String firstName,
                                              boolean enabled,
                                              Map<String, List<String>> attributes,
                                              List<CredentialRepresentation> credentials) {
        static KeycloakUserRepresentation from(CreateUserRequest request) {
            return new KeycloakUserRepresentation(
                    request.userId(),
                    request.username(),
                    request.displayName(),
                    true,
                    attributesFrom(request),
                    credentialsFrom(request));
        }

        private static Map<String, List<String>> attributesFrom(CreateUserRequest request) {
            Map<String, List<String>> attributes = new LinkedHashMap<>();
            addAttribute(attributes, "userId", request.userId());
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
            return List.of(new CredentialRepresentation("password", false, hashedPassword(request.userId())));
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

    private record CredentialRepresentation(String type, boolean temporary, String value) {
    }
}
