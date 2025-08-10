package com.boxclone.musicbox.security;


import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class:
 * Generates JWTs after login
 * Validates JWTs from incoming requests
 * Extracts username and roles from the JWT
 *
 * JWT = JSON Web Token
 * xxxxx.yyyyy.zzzzz
 *
 * | Part          | What it contains                                   | Example (decoded)                        |
 * | ------------- | -------------------------------------------------- | ---------------------------------------- |
 * | **Header**    | Token type (`JWT`) and signing algorithm (`HS256`) | `{ "alg": "HS256", "typ": "JWT" }`       |
 * | **Payload**   | User data (claims): `sub`, `roles`, `exp`, etc.    | `{ "sub": "chris", "roles": ["ADMIN"] }` |
 * | **Signature** | Verifies the token hasn’t been tampered with       | Signed using your secret key             |
 *
 *  User sends:
 *  POST /api/auth/login
 * {
 *   "username": "chris",
 *   "password": "yourPassword"
 * }
 *
 * Server responds with:
 * {
 *   "token": "eyJhbGciOiJIUzI1NiIsInR5..."
 * }
 *
 * User now sends authenticated requests with:
 *
 * GET /api/files
 * Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5...
 *
 * JwtTokenFilter extracts the token
 * JwtTokenProvider verifies the signature + expiration
 * User is authenticated + their roles are added to the Spring Security context
 *
 *
 * | Feature   | Benefit                                       |
 * | --------- | --------------------------------------------- |
 * | Stateless | No need to store sessions on the server       |
 * | Compact   | Easy to send via HTTP headers                 |
 * | Secure    | Signed with a secret; tampering is detectable |
 * | Scalable  | Great for APIs and microservices              |
 */

@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenFilter.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long validityInMs;

    private Key key;

    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String createToken(String username, Set<String> roles) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("roles", roles);

        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityInMs);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }


    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.warn("JWT unsupported: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.warn("JWT malformed: {}", e.getMessage());
            throw e;
        } catch (SecurityException e) {
            log.warn("JWT signature invalid: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.warn("JWT illegal argument: {}", e.getMessage());
            throw e;
        }
        return true;
    }

    public String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody();

        String username = claims.getSubject();
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) claims.get("roles");

        List<GrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new).collect(Collectors.toList());

        return new UsernamePasswordAuthenticationToken(username, "", authorities);
    }
}
