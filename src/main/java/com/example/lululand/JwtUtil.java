package com.example.lululand;

import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Date;

@Component
public class JwtUtil {
    private String SECRET_KEY;
    private final long validityMs = 1000L * 60 * 60 * 24; // 24시간

    @PostConstruct
    public void init() {
        SECRET_KEY = System.getenv("JWT_SECRET");
        if (SECRET_KEY == null) SECRET_KEY = "change-this-default-secret";
    }

    public String generateToken(String subject) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + validityMs);
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY.getBytes())
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(SECRET_KEY.getBytes()).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        Claims claims = Jwts.parser().setSigningKey(SECRET_KEY.getBytes()).parseClaimsJws(token).getBody();
        return claims.getSubject();
    }
}
