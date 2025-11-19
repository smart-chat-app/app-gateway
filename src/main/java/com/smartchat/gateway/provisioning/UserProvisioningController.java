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
@RequestMapping("/user")
public class UserProvisioningController {

    private final UserServiceClient userServiceClient;
    private final KeycloakUserClient userClient;

    public UserProvisioningController(UserServiceClient userServiceClient, KeycloakUserClient userClient) {
        this.userServiceClient = userServiceClient;
        this.userClient = userClient;
    }

    @PostMapping("/create")
    public Mono<ResponseEntity<Void>> create(@Valid @RequestBody CreateUserRequest request) {
        return userServiceClient.createUser(request)
                .then(userClient.createUser(request))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).build());
    }
}
