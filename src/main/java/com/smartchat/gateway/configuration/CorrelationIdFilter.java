package com.smartchat.gateway.configuration;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class CorrelationIdFilter {
    public static final String CORRELATION_ID = "X-Correlation-Id";

    @Bean
    public GlobalFilter correlationFilter() {
        return (exchange, chain) -> {
            var headers = exchange.getRequest().getHeaders();
            var cid = headers.getFirst(CORRELATION_ID);
            if (cid == null || cid.isBlank()) {
                cid = UUID.randomUUID().toString();
            }
            var req = exchange.getRequest().mutate().header(CORRELATION_ID, cid).build();
            return chain.filter(exchange.mutate().request(req).build());
        };
    }
}
