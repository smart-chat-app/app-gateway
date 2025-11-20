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

    /**
     * Creates the user in Keycloak and returns the generated Keycloak userId (subject).
     */
    public Mono<String> createUser(CreateUserRequest request) {
        return adminToken()
                .flatMap(token -> webClient.post()
                        .uri(properties.usersPath())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(KeycloakUserRepresentation.from(request))
                        .exchangeToMono(response -> {
                            if (response.statusCode().is2xxSuccessful()) {
                                String location = response.headers()
                                        .asHttpHeaders()
                                        .getFirst(HttpHeaders.LOCATION);
                                if (location == null) {
                                    return Mono.error(new IllegalStateException(
                                            "Missing Location header from Keycloak user creation"));
                                }
                                String userId = location.substring(location.lastIndexOf('/') + 1);
                                return Mono.just(userId);
                            }
                            return response.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(new IllegalStateException(
                                            "Error creating user in Keycloak: " + body)));
                        }));
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
                    null, // let Keycloak generate the id
                    request.username(),
                    request.displayName(),
                    true,
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
            // For now derive an initial password from the username; adjust if you have a real password flow
            return List.of(new CredentialRepresentation("password", false, hashedPassword(request.username())));
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
