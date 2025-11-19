package com.smartchat.gateway.provisioning;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.smartchat.gateway.configuration.KeycloakAdminProperties;

import reactor.test.StepVerifier;

class KeycloakUserClientTest {

    private WireMockServer server;
    private KeycloakUserClient client;

    @BeforeEach
    void setUp() {
        server = new WireMockServer(0);
        server.start();
        WireMock.configureFor("localhost", server.port());
        KeycloakAdminProperties properties = new KeycloakAdminProperties(
                server.baseUrl(),
                "im",
                "master",
                "admin-cli",
                "admin",
                "admin");
        client = new KeycloakUserClient(properties, WebClient.builder());
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void createsUserAfterFetchingAdminToken() {
        server.stubFor(post(urlEqualTo("/realms/master/protocol/openid-connect/token"))
                .withRequestBody(containing("grant_type=password"))
                .withRequestBody(containing("client_id=admin-cli"))
                .withRequestBody(containing("username=admin"))
                .withRequestBody(containing("password=admin"))
                .willReturn(aResponse().withStatus(200).withBody("{\"access_token\":\"abc\"}")));
        server.stubFor(post(urlEqualTo("/admin/realms/im/users"))
                .withHeader("Authorization", equalTo("Bearer abc"))
                .withRequestBody(equalToJson("""
                        {
                          "username": "alice",
                          "email": "alice@example.com",
                          "firstName": "Alice",
                          "lastName": "User",
                          "enabled": true,
                          "credentials": [
                            { "type": "password", "value": "secret", "temporary": false }
                          ]
                        }
                        """))
                .willReturn(aResponse().withStatus(201)));

        CreateUserRequest request = new CreateUserRequest("alice", "alice@example.com", "Alice", "User", "secret");

        StepVerifier.create(client.createUser(request)).verifyComplete();
    }
}
