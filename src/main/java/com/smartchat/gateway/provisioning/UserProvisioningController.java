package com.smartchat.gateway.provisioning;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/users")
public class UserProvisioningController {

    private final KeycloakUserClient userClient;

    public UserProvisioningController(KeycloakUserClient userClient) {
        this.userClient = userClient;
    }

    @PostMapping("/create")
    public Mono<ResponseEntity<Void>> create(@Valid @RequestBody CreateUserRequest request) {
        return userClient.createUser(request)
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).build());
    }
}
