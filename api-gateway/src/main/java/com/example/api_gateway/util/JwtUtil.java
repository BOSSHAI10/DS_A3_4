package com.example.api_gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
public class JwtUtil {

    // Aceasta trebuie să fie EXACT aceeași cheie ca în Auth Service
    @Value("${jwt.secret}")
    private String secret;

    public void validateToken(String token) {
        // Dacă token-ul e invalid sau expirat, această metodă va arunca o excepție
        Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token);
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token).getBody();
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret); // Sau folosim cheia raw dacă în auth e raw
        // NOTĂ: Dacă în auth-service ai folosit string simplu, aici trebuie adaptat.
        // Pentru HS256 standard, transformăm string-ul în bytes:
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Aceasta este metoda care lipsea și care generează eroarea
    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}