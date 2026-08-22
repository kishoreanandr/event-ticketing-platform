package com.eventticketing.user.service;

import com.eventticketing.user.security.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expiration;
    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration){
        this.secretKey= Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secret)
        );
        this.expiration=expiration;
    }

    public String generateToken(CustomUserDetails userDetails){
        Date issuedAt=new Date();

        Date expirationTime=new Date(issuedAt.getTime()+expiration);

        List<String> roles=userDetails
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts
                .builder()
                .subject(userDetails.getUsername())
                .claim("userId",userDetails.getUserId())
                .claim("roles",roles)
                .issuedAt(issuedAt)
                .expiration(expirationTime)
                .signWith(secretKey)
                .compact();
    }
    public String extractUserName(String token){
        return extractAllClaims(token).getSubject();
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token) {
        try{
            extractAllClaims(token);
            return true;
        }catch (Exception exception){
            return false;
        }
    }
    public List<String> extractRoles(String token){
        Claims claims=extractAllClaims(token);
        return claims.get("roles",List.class);
    }
}
