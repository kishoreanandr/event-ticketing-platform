package com.eventticketing.common_security.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

public class JwtTokenService {

    private final SecretKey secretKey;

    public JwtTokenService(String secret) {

        this.secretKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secret)
        );
    }

    public Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {

        return extractAllClaims(token)
                .getSubject();
    }

    public List<String> extractRoles(String token) {

        Object roles = extractAllClaims(token)
                .get("roles");

        if (roles instanceof List<?> list) {

            return list.stream()
                    .map(Object::toString)
                    .toList();
        }

        return List.of();
    }

    public boolean isTokenValid(String token) {

        try {

            extractAllClaims(token);

            return true;

        } catch (Exception exception) {

            return false;
        }
    }
    public Long extractUserId(String token){
        Object userId= extractAllClaims(token)
                .get("userId");
        if (userId==null)
            return null;
        if(userId instanceof Number number){
            return number.longValue();
        }
        return Long.valueOf(userId.toString());
    }

    public Date extractExpiration(String token) {

        return extractAllClaims(token)
                .getExpiration();
    }
}