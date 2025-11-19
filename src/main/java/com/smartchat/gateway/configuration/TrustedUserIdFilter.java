package com.smartchat.gateway.configuration;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Locale;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class TrustedUserIdFilter implements GlobalFilter, Ordered {

    static final String USER_ID_HEADER = "X-User-Id";
    static final int USER_ID_LENGTH = 26;

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest sanitized = exchange.getRequest().mutate()
                .headers(headers -> headers.remove(USER_ID_HEADER))
                .build();
        ServerWebExchange sanitizedExchange = exchange.mutate().request(sanitized).build();

        return exchange.getPrincipal()
                .cast(Authentication.class)
                .map(this::resolveUserId)
                .map(userId -> sanitizedExchange.mutate()
                        .request(sanitizedExchange.getRequest().mutate().header(USER_ID_HEADER, userId).build())
                        .build())
                .defaultIfEmpty(sanitizedExchange)
                .flatMap(chain::filter);
    }

    private String resolveUserId(Authentication authentication) {
        String raw;
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            raw = jwtAuthenticationToken.getToken().getSubject();
        } else {
            raw = authentication.getName();
        }
        return safeUserId(raw);
    }

    private String safeUserId(String subject) {
        if (subject == null || subject.isBlank()) {
            return "anonymous";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(subject.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8));
            String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
            return encoded.substring(0, Math.min(USER_ID_LENGTH, encoded.length()));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Missing SHA-256 algorithm", e);
        }
    }
}
