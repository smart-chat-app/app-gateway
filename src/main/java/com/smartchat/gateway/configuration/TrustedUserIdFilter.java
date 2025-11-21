package com.smartchat.gateway.configuration;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class TrustedUserIdFilter implements GlobalFilter, Ordered {

    private static final String USER_ID_HEADER = "X-User-Id";

    @Override
    public int getOrder() {
        // run after UserIdForwardingFilter (-10), but before routing
        return -5;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        HttpMethod method = exchange.getRequest().getMethod();

        // 1. Skip enforcement for public endpoints
        if (isPublic(path, method)) {
            return chain.filter(exchange);
        }

        // 2. Enforce presence of X-User-Id
        String userId = exchange.getRequest().getHeaders().getFirst(USER_ID_HEADER);
        if (userId == null || userId.isBlank()) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Missing X-User-Id header"));
        }

        return chain.filter(exchange);
    }

    private boolean isPublic(String path, HttpMethod method) {
        if (method == HttpMethod.OPTIONS) {
            return true;
        }

        // Actuator / health
        if (path.startsWith("/actuator")) {
            return true;
        }

        // User provisioning endpoint (no auth, creates Keycloak + user-service)
        if (path.startsWith("/user/create")) {
            return true;
        }

        // Users health (proxied to users-service)
        if (path.startsWith("/users/health")) {
            return true;
        }

        // API docs / Swagger
        return path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui");
    }
}
