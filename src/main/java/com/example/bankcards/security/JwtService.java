package com.example.bankcards.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {
    
    @Value("${jwt.secret}")
    private String SECRET;

    @Value("${jwt.accessExpiration}")
    private Duration ACCESS_EXPIRATION;

    @Value("${jwt.refreshExpiration}")
    private Duration REFRESH_EXPIRATION;

    public String generateAccessToken(UserDetails details) {
        return buildToken(new HashMap<>(), details, ACCESS_EXPIRATION);
    }

    public String generateRefreshToken(UserDetails details) {
        return buildToken(new HashMap<>(), details, REFRESH_EXPIRATION);
    }

    public String generateRefreshToken(Map<String, Object> claims, UserDetails details) {
        return buildToken(claims, details, REFRESH_EXPIRATION);
    }

    private String buildToken(Map<String, Object> claims, UserDetails details, Duration expiration) {
        return Jwts
            .builder()
            .claims(claims)
            .subject(details.getUsername())
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + expiration.toMillis()))
            .signWith(getSigningKey(), Jwts.SIG.HS256)
            .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();    
    }

    private SecretKey getSigningKey() {
        byte[] bytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(bytes);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Duration getAccessExpiration() {
        return ACCESS_EXPIRATION;
    }

    public Duration getRefreshExpiration() {
        return REFRESH_EXPIRATION;
    }

    public Instant getAccessExpirationInstant() {
        return Instant.now().plus(ACCESS_EXPIRATION);
    }

    public Instant getRefreshExpirationInstant() {
        return Instant.now().plus(REFRESH_EXPIRATION);
    }
}