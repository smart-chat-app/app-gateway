/*
package com.smartchat.gateway.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class MeDebugFilter implements GlobalFilter, Ordered {

    @Override
    public int getOrder() {
        // Run VERY early, before everything (-100)
        return -100;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // Only intercept /users/me/** for debugging
        if (!path.startsWith("/users/me")) {
            return chain.filter(exchange);
        }

        String auth = request.getHeaders().getFirst("Authorization");
        String xUserId = request.getHeaders().getFirst("X-User-Id");

        log.info("MeDebugFilter: incoming request path={}, Authorization={}, X-User-Id={}",
                path,
                auth != null ? (auth.length() > 20 ? auth.substring(0, 20) + "..." : auth) : "null",
                xUserId);

        // **DO NOT** call downstream yet – just return a synthetic response
        exchange.getResponse().setStatusCode(HttpStatus.OK);
        byte[] body = ("MeDebugFilter OK, path=" + path).getBytes();
        return chain.filter(exchange)
                .doOnError(e -> log.error("MeDebugFilter ERROR after routing → {}", e.getMessage(), e));
    }
}
*/
