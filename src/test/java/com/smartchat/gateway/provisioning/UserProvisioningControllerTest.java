package com.smartchat.gateway.provisioning;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.smartchat.gateway.configuration.SecurityConfig;

import reactor.core.publisher.Mono;

@WebFluxTest(controllers = UserProvisioningController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost/.well-known/jwks.json",
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost/issuer"
})
class UserProvisioningControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private UserServiceClient userServiceClient;

    @MockBean
    private KeycloakUserClient keycloakUserClient;

    @Test
    void allowsUserCreationWithoutAuthenticationAndCallsClients() {
        CreateUserRequest request = new CreateUserRequest(
                "abc-1200709",
                "user_abc124",
                "frank3",
                "test-test",
                "https://example.com/avatar.png");

        when(userServiceClient.createUser(request)).thenReturn(Mono.empty());
        when(keycloakUserClient.createUser(request)).thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/user/create")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();

        verify(userServiceClient).createUser(request);
        verify(keycloakUserClient).createUser(request);
    }
}
