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

    /**
     * Public endpoint used to provision a new user:
     *  1) Create the user in Keycloak
     *  2) Create the user in the users-service DB using the Keycloak userId as userId
     */
    @PostMapping("/create")
    public Mono<ResponseEntity<Void>> create(@Valid @RequestBody CreateUserRequest request) {
        return userClient.createUser(request)
                .flatMap(keycloakUserId -> userServiceClient.createUser(keycloakUserId, request))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).build());
    }
}
