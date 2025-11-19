package com.smartchat.gateway.provisioning;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
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
                        .onStatus(status -> status.isError(), response -> response.createException())
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
                .onStatus(status -> status.isError(), response -> response.createException())
                .bodyToMono(TokenResponse.class)
                .map(TokenResponse::accessToken);
    }

    private record TokenResponse(String access_token) {
        String accessToken() {
            return access_token;
        }
    }

    private record KeycloakUserRepresentation(String username,
                                              String email,
                                              String firstName,
                                              String lastName,
                                              boolean enabled,
                                              List<Credential> credentials) {
        static KeycloakUserRepresentation from(CreateUserRequest request) {
            return new KeycloakUserRepresentation(
                    request.username(),
                    request.email(),
                    request.firstName(),
                    request.lastName(),
                    true,
                    List.of(new Credential("password", request.password(), false)));
        }
    }

    private record Credential(String type, String value, boolean temporary) {
    }
}
