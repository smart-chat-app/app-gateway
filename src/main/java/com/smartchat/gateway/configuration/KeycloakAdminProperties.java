package com.smartchat.gateway.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "keycloak.admin")
public record KeycloakAdminProperties(
        String baseUrl,
        String realm,
        String adminRealm,
        String clientId,
        String username,
        String password) {

    public String tokenPath() {
        return "/realms/" + adminRealm + "/protocol/openid-connect/token";
    }

    public String usersPath() {
        return "/admin/realms/" + realm + "/users";
    }
}
