package com.gateway.smartchat.test;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.http.MediaType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@TestPropertySource(locations = "classpath:application-test.yaml",
        properties = {
                "KEYCLOAK_JWKS=http://localhost:9997/jwks.json",
                "ISSUER_URI=http://localhost:9996/realms/test",
                "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:9996/realms/test",
                "spring.cloud.gateway.routes[0].id=users",
                "spring.cloud.gateway.routes[0].uri=forward:/__echo",
                "spring.cloud.gateway.routes[0].predicates[0]=Path=/users/**",

                "spring.cloud.gateway.routes[1].id=messages",
                "spring.cloud.gateway.routes[1].uri=forward:/__echo",
                "spring.cloud.gateway.routes[1].predicates[0]=Path=/messages/**",

                // make resolver simple if something else tries resolution
                "spring.cloud.gateway.httpclient.resolver=default",
        })
@Import(TestSecurityConfig.class)
public class GatewayE2ETest {

    static MockWebServer users = new MockWebServer();
    static MockWebServer messages = new MockWebServer();

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) throws Exception {
        users.start();
        messages.start();
        r.add("wiremock.users.port", () -> users.getPort());
        r.add("wiremock.messages.port", () -> messages.getPort());
    }

    @AfterAll
    static void down() throws Exception {
        users.shutdown();
        messages.shutdown();
    }

    @LocalServerPort
    int port;
    @Autowired
    WebTestClient webTestClient;

    @BeforeEach
    void enqueue() {
        users.enqueue(new MockResponse()
                .setBody("{\"id\":\"u1\",\"name\":\"Ada\"}")
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200));
        messages.enqueue(new MockResponse()
                .setBody("{\"id\":\"42\",\"text\":\"hello\"}")
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200));
    }

    @Test
    void forwards_users() {
        webTestClient.get().uri("/users/me")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody().jsonPath("$.path").value(p -> ((String)p).endsWith("/users/me"));
    }

    @Test
    void forwards_messages() {
        webTestClient.get().uri("/messages/42")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody().jsonPath("$.path").value(p -> ((String)p).endsWith("/messages/42"));
    }
}
