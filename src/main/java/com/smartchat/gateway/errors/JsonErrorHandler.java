package com.smartchat.gateway.errors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@Order(-2)
@Primary
public class JsonErrorHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper om = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        var resp = exchange.getResponse();
        if (resp.isCommitted()) return Mono.error(ex);

        var status = HttpStatus.INTERNAL_SERVER_ERROR;
        if (ex instanceof ResponseStatusException rse) {
            status = HttpStatus.valueOf(rse.getStatusCode().value());
        }

        resp.setStatusCode(status);
        resp.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        var body = Map.of(
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "path", exchange.getRequest().getPath().value(),
                "message", ex.getMessage()
        );
        try {
            var bytes = om.writeValueAsBytes(body);
            var buffer = resp.bufferFactory().wrap(bytes);
            return resp.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            var fallback = resp.bufferFactory().wrap("{\"error\":\"unexpected\"}".getBytes(StandardCharsets.UTF_8));
            return resp.writeWith(Mono.just(fallback));
        }
    }
}
