package com.smartchat.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;

import com.smartchat.gateway.configuration.TrustedUserIdFilter;

import reactor.core.publisher.Mono;

class TrustedUserIdFilterTest {

    private final TrustedUserIdFilter filter = new TrustedUserIdFilter();

    @Test
    void removesClientProvidedHeaderAndAddsTrustedOne() {
        ServerHttpRequest request = MockServerHttpRequest.get("/chats")
                .header(TrustedUserIdFilter.USER_ID_HEADER, "spoofed")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        exchange.setPrincipal(Mono.just(new TestingAuthenticationToken("sub-123", null)));

        GatewayFilterChain chain = new NoopChain();
        filter.filter(exchange, chain).block();

        HttpHeaders headers = chain.captured.getHeaders();
        assertThat(headers.getFirst(TrustedUserIdFilter.USER_ID_HEADER))
                .isNotEqualTo("spoofed")
                .hasSizeLessThanOrEqualTo(TrustedUserIdFilter.USER_ID_LENGTH);
    }

    @Test
    void leavesHeaderAbsentWhenUnauthenticated() {
        ServerHttpRequest request = MockServerHttpRequest.get("/chats")
                .header(TrustedUserIdFilter.USER_ID_HEADER, "spoofed")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilterChain chain = new NoopChain();
        filter.filter(exchange, chain).block();

        assertThat(chain.captured.getHeaders().containsKey(TrustedUserIdFilter.USER_ID_HEADER)).isFalse();
    }

    private static final class NoopChain implements GatewayFilterChain {
        private ServerHttpRequest captured;

        @Override
        public Mono<Void> filter(ServerWebExchange exchange) {
            this.captured = exchange.getRequest();
            return Mono.empty();
        }
    }
}
