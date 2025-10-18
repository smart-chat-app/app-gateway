package com.gateway.smartchat.test;

// RouteLocatorConfigTest.java
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ssl.SslAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.config.GatewayAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;


@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {
                GatewayTestApplication.class,   // <- provides @SpringBootConfiguration
                TestRouteConfig.class,
                TestEchoController.class,
                GatewayAutoConfiguration.class, // <- provides RouteLocatorBuilder
                SslAutoConfiguration.class      // <- provides SslBundles
        }
)
@AutoConfigureWebTestClient
@TestPropertySource(properties = {
        "KEYCLOAK_JWKS=http://localhost:9997/jwks.json",
        "ISSUER_URI=http://localhost:9996/realms/test",
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:9996/realms/test"
})
class RouteLocatorConfigTest {

    @Autowired WebTestClient webTestClient;

    @Test
    void usersRoute_matchesPathAndForwards() {
        webTestClient.get().uri("/users/me")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.path").value(p -> ((String)p).endsWith("/users/me"));
    }

    @Test
    void messagesRoute_matchesPathAndForwards() {
        webTestClient.get().uri("/messages/123")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.path").value(p -> ((String)p).endsWith("/messages/123"));
    }
}
