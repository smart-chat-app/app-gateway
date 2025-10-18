package com.gateway.smartchat.test;

import org.springframework.http.HttpHeaders;

public final class TestUtils {
    private TestUtils() {}

    public static HttpHeaders bearer(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        return headers;
    }
}
