package com.gateway.smartchat.test;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class GatewayE2ETest {

    static WireMockServer users = new WireMockServer(wireMockConfig().dynamicPort());
    static WireMockServer messages = new WireMockServer(wireMockConfig().dynamicPort());
    static WireMockServer auth = new WireMockServer(wireMockConfig().dynamicPort());

    @DynamicPropertySource
    static void wireProps(DynamicPropertyRegistry r) {
        users.start();
        messages.start();
        r.add("wiremock.users.port", users::port);
        r.add("wiremock.messages.port", messages::port);
        auth.start();
        r.add("KEYCLOAK_JWKS", () -> "http://localhost:" + auth.port() + "/jwks.json");
    }


    @LocalServerPort
    int port;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeAll
    static void stubs() {
        users.stubFor(WireMock.get("/users/me").willReturn(
            WireMock.okJson("{\"id\":\"u1\",\"name\":\"Ada\"}")
        ));
        messages.stubFor(WireMock.get("/messages/42").willReturn(
            WireMock.okJson("{\"id\":\"42\",\"text\":\"hello\"}")
        ));
    }

    @AfterAll
    static void shutdown() {
        users.stop();
        messages.stop();
    }

    @Test
    void forwardsUsers_me_andReturnsJson() {
        webTestClient
            .mutate().baseUrl("http://localhost:" + port).build()
            .get().uri("/users/me")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.name").isEqualTo("Ada");
    }

    @Test
    void forwardsMessages_andReturnsJson() {
        webTestClient
            .mutate().baseUrl("http://localhost:" + port).build()
            .get().uri("/messages/42")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id").isEqualTo("42");
    }
}
