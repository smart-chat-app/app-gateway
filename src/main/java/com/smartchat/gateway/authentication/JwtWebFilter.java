/*
package com.smartchat.gateway.authentication;

import lombok.AllArgsConstructor;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

@Component
@AllArgsConstructor
public class JwtWebFilter implements WebFilter {

    private final JWTService jwtService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        //TODO Implement method
        String token = getToken(exchange.getRequest());

        if (token != null && jwtService.isTokenValid(token)) {
            String username = jwtService.extractEmail(token);
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    username, null, List.of());
            return chain.filter(exchange).contextWrite(
                    ReactiveSecurityContextHolder.withAuthentication(auth));
        }

        return chain.filter(exchange);
    }

    private String getToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst("Authorization");
        if(Objects.nonNull(authHeader) && authHeader.startsWith("Bearer: ")){
            return authHeader.substring(7);
        }
        return null;
    }
}
*/
