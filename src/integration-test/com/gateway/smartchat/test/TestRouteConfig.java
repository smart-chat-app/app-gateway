package com.gateway.smartchat.test;

// TestRouteConfig.java
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.ConfigurableApplicationContext;

@TestConfiguration
public class TestRouteConfig {

    // Use "forward:" so we don't need any real downstream HTTP during the test
    @Bean
    RouteLocator testRoutes(RouteLocatorBuilder rlb) {
        return rlb.routes()
                .route("users", r -> r.path("/users/**").uri("forward:/__echo"))
                .route("messages", r -> r.path("/messages/**").uri("forward:/__echo"))
                .build();
    }
}
