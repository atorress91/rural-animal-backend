package com.project.demo.logic.entity.auth;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

@Component
public class TokenStorage {
    private final Map<String, String> tokenMap = new ConcurrentHashMap<>();
    private static final long TOKEN_EXPIRATION = 300000;

    public String storeToken(String token) {
        String sessionId = UUID.randomUUID().toString();
        tokenMap.put(sessionId, token);

        new Thread(() -> {
            try {
                Thread.sleep(TOKEN_EXPIRATION);
                tokenMap.remove(sessionId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        return sessionId;
    }

    public String getToken(String sessionId) {
        return tokenMap.get(sessionId);
    }

    public void removeToken(String sessionId) {
        tokenMap.remove(sessionId);
    }
}