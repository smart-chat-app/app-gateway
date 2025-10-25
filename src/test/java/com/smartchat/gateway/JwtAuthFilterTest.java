
package com.smartchat.gateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class JwtAuthFilterTest {

    interface JwtVerifier {
        Mono<Boolean> verify(String token);
    }

    static class JwtAuthFilter implements org.springframework.web.server.WebFilter {
        private final JwtVerifier verifier;

        JwtAuthFilter(JwtVerifier verifier) {
            this.verifier = verifier;
        }

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, org.springframework.web.server.WebFilterChain chain) {
            String auth = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (auth == null || !auth.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            String token = auth.substring(7);
            return verifier.verify(token)
                .flatMap(valid -> valid ? chain.filter(exchange)
                        : Mono.fromRunnable(() -> exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED))
                              .then(exchange.getResponse().setComplete()));
        }
    }

    private JwtVerifier verifier;
    private JwtAuthFilter filter;

    @BeforeEach
    void setUp() {
        verifier = Mockito.mock(JwtVerifier.class);
        filter = new JwtAuthFilter(verifier);
    }

    @Test
    void rejectsWhenNoBearer() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/users/me").build());
        filter.filter(exchange, e -> Mono.empty()).block();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void rejectsWhenInvalidToken() {
        when(verifier.verify(any())).thenReturn(Mono.just(false));
        var req = MockServerHttpRequest.get("/users/me").header("Authorization", "Bearer bad").build();
        var exchange = MockServerWebExchange.from(req);
        filter.filter(exchange, e -> Mono.empty()).block();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void allowsWhenValidToken() {
        when(verifier.verify(any())).thenReturn(Mono.just(true));
        var req = MockServerHttpRequest.get("/users/me").header("Authorization", "Bearer good").build();
        var exchange = MockServerWebExchange.from(req);
        var result = filter.filter(exchange, e -> Mono.empty()).block();
        assertThat(result).isNull(); // chain completed normally
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }
}
