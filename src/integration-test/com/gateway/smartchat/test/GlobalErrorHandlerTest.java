package com.gateway.smartchat.test;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = {
        "KEYCLOAK_JWKS=http://localhost:9997/jwks.json",
        "ISSUER_URI=http://localhost:9996/realms/test"
})
@WebFluxTest(controllers = GlobalErrorHandlerTest.Ctrl.class)
public class GlobalErrorHandlerTest {

    @Configuration
    static class Cfg {
        @Bean
        WebExceptionHandler globalHandler() {
            return new JsonErrorHandler();
        }
    }

    @RestController
    static class Ctrl {
        @GetMapping("/boom")
        Mono<String> boom() {
            return Mono.error(new IllegalStateException("boom"));
        }
    }

    @Component
    static class JsonErrorHandler implements WebExceptionHandler {
        @Override
        public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            var buf = exchange.getResponse().bufferFactory()
                    .wrap(("{\"error\":\"" + ex.getClass().getSimpleName() + "\"}").getBytes());
            return exchange.getResponse().writeWith(Mono.just(buf));
        }
    }

    @Test
    void returnsProblemJson() {
        WebTestClient.bindToController(new Ctrl()).controllerAdvice(new Cfg().globalHandler()).build()
            .get().uri("/boom")
            .exchange()
            .expectStatus().is5xxServerError()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.error").isEqualTo("IllegalStateException");
    }
}
