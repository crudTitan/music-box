package com.boxclone.musicbox.controller;

import com.boxclone.musicbox.dto.LoginRequest;
import com.boxclone.musicbox.dto.RegisterRequest;
import com.boxclone.musicbox.entity.RoleEntity;
import com.boxclone.musicbox.entity.UserEntity;
import com.boxclone.musicbox.repository.RoleRepository;
import com.boxclone.musicbox.repository.UserRepository;
import com.boxclone.musicbox.security.JwtTokenProvider;
import com.boxclone.musicbox.service.SpotifyAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;

import java.util.*;

/**
 * Handles:
 * /api/auth/register
 * /api/auth/login
 *
 */

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authManager;
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    private SpotifyAuthService spotifyAuthService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepo.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        RoleEntity userRole = roleRepo.findByName("ROLE_USER")
                .orElseGet(() -> roleRepo.save(RoleEntity.builder().name("ROLE_USER").build()));

        UserEntity newUser = UserEntity.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(userRole))
                .build();

        userRepo.save(newUser);
        return ResponseEntity.ok("User registered successfully");
    }



    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            log.debug("Enter, Login request for user: {}", request.getUsername());

            var userPwdAuthToken = new UsernamePasswordAuthenticationToken(
                    request.getUsername(), request.getPassword());

            Authentication auth = authManager.authenticate(userPwdAuthToken);

            Set<String> roles = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());

            String token = jwtTokenProvider.createToken(request.getUsername(), roles);

            log.debug("Login successful for user: {}", request.getUsername());

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "username", request.getUsername(),
                    "roles", roles
            ));

        } catch (UsernameNotFoundException e) {
            log.debug("Failed to find user {}: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));
        } catch (BadCredentialsException e) {
            log.debug("Bad credentials for user {}: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));
        } catch (AuthenticationException e) {
            log.error("Unexpected authentication error", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication failed"));
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(HttpServletRequest request) {
        // JwtTokenFilter would normally already run here before this method
        //return ResponseEntity.ok().build(); // 200 means valid
        return ResponseEntity.ok("Validated"); // 200 means valid
    }



    @GetMapping("/login/oauth2/spotify")
    public ResponseEntity<String> loginSpotify(
            @RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "error", required = false) String error) {

        if (error != null) {
            return ResponseEntity.badRequest().body("Spotify error: " + error);
        }

        if (code == null) {
            return ResponseEntity.badRequest().body("No authorization code provided");
        }

        // Exchange code for access & refresh tokens
        String jwtToken = spotifyAuthService.exchangeCodeForToken(code);

        // Return JWT to frontend
        return ResponseEntity.ok(jwtToken);
    }

}
