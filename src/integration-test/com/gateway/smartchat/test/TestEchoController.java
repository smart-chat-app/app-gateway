package com.gateway.smartchat.test;

import java.util.Map;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestEchoController {
    @RequestMapping("/__echo/**")
    Map<String, Object> echo(ServerHttpRequest req) {
        return Map.of("path", req.getURI().getPath());
    }
}
