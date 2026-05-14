package com.codearena.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(@Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
        this.expirationMs = expirationMs;
    }

    public String generateToken(String username, Long userId, String role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(username)
                .claim("uid", userId)
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public String getUsername(String token) {
        return parse(token).getSubject();
    }

    public Long getUserId(String token) {
        Object uid = parse(token).get("uid");
        if (uid instanceof Number n)
            return n.longValue();
        return null;
    }

    public String getRole(String token) {
        Object role = parse(token).get("role");
        return role == null ? "USER" : role.toString();
    }
}
