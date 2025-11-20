package com.smartchat.gateway.provisioning;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import com.smartchat.gateway.configuration.UsersServiceProperties;

import reactor.core.publisher.Mono;

@Component
public class UserServiceClient {

    private final UsersServiceProperties properties;
    private final WebClient webClient;

    public UserServiceClient(UsersServiceProperties properties, WebClient.Builder builder) {
        this.properties = properties;
        this.webClient = builder.baseUrl(properties.baseUrl()).build();
    }

    /**
     * Create the user in the users-service database using the Keycloak userId.
     */
    public Mono<Void> createUser(String userId, CreateUserRequest request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("userId", userId);
        payload.put("username", request.username());
        payload.put("displayName", request.displayName());
        if (request.bio() != null) {
            payload.put("bio", request.bio());
        }
        if (request.avatarUrl() != null) {
            payload.put("avatarUrl", request.avatarUrl());
        }

        return webClient.post()
                .uri(properties.createPath())   // <- this should now be "/users/create"
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .onStatus(HttpStatusCode::isError, ClientResponse::createException)
                .bodyToMono(Void.class);
    }
}
