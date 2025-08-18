package com.boxclone.musicbox.service;

public interface JwtService {
    String generateToken(String userId);      // Create JWT for a user
    boolean validateToken(String token);      // Optional: check if JWT is valid
    String getUserIdFromToken(String token);  // Optional: extract user ID from JWT
}

