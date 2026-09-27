package com.skillexchange.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService {

    // token -> userId  (kept in memory; resets when the app restarts)
    private final Map<String, Long> activeTokens = new ConcurrentHashMap<>();

    public String issueToken(Long userId) {
        String token = UUID.randomUUID().toString();
        activeTokens.put(token, userId);
        return token;
    }

    public Optional<Long> resolve(String token) {
        if (token == null) return Optional.empty();
        // allow clients to send either "Bearer <token>" or the raw token
        String cleaned = token.startsWith("Bearer ") ? token.substring(7) : token;
        return Optional.ofNullable(activeTokens.get(cleaned));
    }

    public void revoke(String token) {
        if (token == null) return;
        String cleaned = token.startsWith("Bearer ") ? token.substring(7) : token;
        activeTokens.remove(cleaned);
    }
}
