package com.shubham.stockmonitoring.auth.service;

import com.shubham.stockmonitoring.auth.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static io.jsonwebtoken.Jwts.*;

@Service
public class JwtService {
    
    @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String secretKey;
    
    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;
    
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    public String generateToken(User user) {
        return generateToken(new HashMap<>(), user);
    }
    
    public String generateToken(Map<String, Object> extraClaims, User user) {
        return buildToken(extraClaims, user, jwtExpiration);
    }
    
    private String buildToken(
            Map<String, Object> extraClaims,
            User user,
            long expiration
    ) {
        return builder()
                .claims(extraClaims)
                .subject(user.getEmail())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }
    
    public boolean isTokenValid(String token, User user) {
        final String email = extractEmail(token);
        return (email.equals(user.getName())) && !isTokenExpired(token);
    }
    
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
