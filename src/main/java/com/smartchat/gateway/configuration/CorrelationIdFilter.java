package com.smartchat.gateway.configuration;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

@Component
public class CorrelationIdFilter implements GlobalFilter {

    private static final String HDR = "X-Request-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var req = exchange.getRequest();
        var id = Optional.ofNullable(req.getHeaders().getFirst(HDR))
                .orElse(UUID.randomUUID().toString());
        var mutated = exchange.mutate().request(b -> b.headers(h -> h.set(HDR, id))).build();
        return chain.filter(mutated);
    }
}
