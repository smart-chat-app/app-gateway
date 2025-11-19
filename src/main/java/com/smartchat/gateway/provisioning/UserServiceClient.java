package com.smartchat.gateway.provisioning;

import org.springframework.http.MediaType;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
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

    public Mono<Void> createUser(CreateUserRequest request) {
        return webClient.post()
                .uri(properties.createPath())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.createException())
                .bodyToMono(Void.class);
    }
}
