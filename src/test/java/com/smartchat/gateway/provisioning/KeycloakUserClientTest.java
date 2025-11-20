package com.smartchat.gateway.provisioning;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.smartchat.gateway.configuration.KeycloakAdminProperties;

import reactor.test.StepVerifier;

@Disabled
class KeycloakUserClientTest {
/*
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
                          "username": "user_abc124",
                          "firstName": "frank3",
                          "enabled": true,
                          "attributes": {
                            "userId": ["abc-1200709"],
                            "displayName": ["frank3"],
                            "bio": ["test-test"],
                            "avatarUrl": ["https://example.com/avatar.png"]
                          },
                          "credentials": [
                            {
                              "type": "password",
                              "temporary": false,
                              "value": "jlUCWIGMrnSavzpqpC600tIGxuiQBRNiKMl4lt50MRM"
                            }
                          ]
                        }
                        """))
                .willReturn(aResponse().withStatus(201)));

        CreateUserRequest request = new CreateUserRequest(
                "abc-1200709",
                "user_abc124",
                "frank3",
                "test-test",
                "https://example.com/avatar.png");

        StepVerifier.create(client.createUser(request)).verifyComplete();
    }*/
}
