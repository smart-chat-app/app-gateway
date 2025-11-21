package com.smartchat.gateway.configuration;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
public class UserIdPropagationConfig {

    public static final String USER_ID_HEADER = "X-User-Id";

    @Bean
    public GlobalFilter userIdForwardingFilter() {
        return new UserIdForwardingFilter();
    }

    static class UserIdForwardingFilter implements GlobalFilter, Ordered {

        @Override
        public int getOrder() {
            // Run early, before TrustedUserIdFilter
            return -10;
        }

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            // 1. Start from a request where we *always* remove any client-sent X-User-Id
            ServerHttpRequest.Builder baseBuilder = exchange.getRequest().mutate();
            baseBuilder.headers(headers -> headers.remove(USER_ID_HEADER));

            ServerHttpRequest baseRequest = baseBuilder.build();
            ServerWebExchange baseExchange = exchange.mutate().request(baseRequest).build();

            // 2. Try to get the authenticated principal (if any)
            return baseExchange.getPrincipal()
                    .cast(Authentication.class)
                    .filter(auth -> auth instanceof JwtAuthenticationToken)
                    .cast(JwtAuthenticationToken.class)
                    .map(jwtAuth -> {
                        String sub = jwtAuth.getToken().getSubject();
                        if (sub != null && !sub.isBlank()) {
                            // Add X-User-Id header derived from JWT sub
                            ServerHttpRequest reqWithUserId = baseRequest.mutate()
                                    .header(USER_ID_HEADER, sub)
                                    .build();
                            return baseExchange.mutate().request(reqWithUserId).build();
                        }
                        // No sub or blank → fall back to baseExchange without header
                        return baseExchange;
                    })
                    // 3. If there is no principal at all (unauthenticated), just proceed with baseExchange
                    .defaultIfEmpty(baseExchange)
                    // 4. Continue the filter chain with the chosen exchange
                    .flatMap(chain::filter);
        }
    }
}
