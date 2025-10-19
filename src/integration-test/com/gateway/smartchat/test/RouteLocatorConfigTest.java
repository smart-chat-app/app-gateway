package com.gateway.smartchat.test;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ssl.SslAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.config.GatewayAutoConfiguration;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;


@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {
                GatewayTestApplication.class,
                TestRouteConfig.class,
                TestEchoController.class,
                GatewayAutoConfiguration.class,
                SslAutoConfiguration.class
        }
)
@AutoConfigureWebTestClient
@TestPropertySource(properties = {
        "KEYCLOAK_JWKS=http://localhost:9997/jwks.json",
        "ISSUER_URI=http://localhost:9996/realms/test",
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:9996/realms/test"
})
public class RouteLocatorConfigTest {

    @Autowired WebTestClient webTestClient;
    @Mock
    ReactiveJwtDecoder jwtDecoder;

    @Test
    void usersRoute_matchesPathAndForwards() {
        webTestClient.mutateWith(mockJwt()) // <- pretend the user is authenticated
                .get().uri("/users/me")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.path").value(p -> ((String)p).endsWith("/users/me"));
    }

    @Test
    void messagesRoute_matchesPathAndForwards() {
        webTestClient.mutateWith(mockJwt())
                .get().uri("/messages/123")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.path").value(p -> ((String)p).endsWith("/messages/123"));
    }
}
