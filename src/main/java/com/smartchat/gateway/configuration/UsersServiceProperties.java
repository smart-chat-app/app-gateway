package com.smartchat.gateway.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "users.service")
public record UsersServiceProperties(String baseUrl, String createPath) {
}
