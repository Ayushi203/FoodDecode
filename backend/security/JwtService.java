package com.fooddecode.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey key;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration-ms}") long accessExpirationMs,
            @Value("${jwt.refresh-expiration-ms}") long refreshExpirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public String createAccessToken(String email, String role) {
        return buildToken(email, Map.of("role", role, "typ", "access"), accessExpirationMs);
    }

    public String createRefreshToken(String email) {
        return buildToken(email, Map.of("typ", "refresh"), refreshExpirationMs);
    }

    public String createPasswordResetToken(String email) {
        return buildToken(email, Map.of("typ", "reset"), 10 * 60 * 1000L);
    }

    public String extractEmail(String token) {
        return parse(token).getSubject();
    }

    public String extractType(String token) {
        Object typ = parse(token).get("typ");
        return typ == null ? "" : typ.toString();
    }

    public boolean isValid(String token, String expectedType) {
        try {
            Claims claims = parse(token);
            return expectedType.equals(String.valueOf(claims.get("typ")))
                    && claims.getExpiration().toInstant().isAfter(Instant.now());
        } catch (Exception ex) {
            return false;
        }
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }

    private String buildToken(String email, Map<String, Object> claims, long ttlMs) {
        Instant now = Instant.now();
        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(ttlMs)))
                .signWith(key)
                .compact();
    }

    private Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
