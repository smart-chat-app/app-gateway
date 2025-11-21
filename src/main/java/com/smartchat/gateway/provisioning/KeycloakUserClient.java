package com.smartchat.gateway.provisioning;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartchat.gateway.configuration.KeycloakAdminProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
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
        // Uses admin credentials from KeycloakAdminProperties (grant_type=password)
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
}
