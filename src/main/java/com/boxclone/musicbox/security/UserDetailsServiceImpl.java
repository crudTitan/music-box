package com.boxclone.musicbox.security;


import com.boxclone.musicbox.entity.UserEntity;
import com.boxclone.musicbox.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * Spring Security needs this to load the authenticated user’s
 * details (used during login and for authentication recovery from the JWT).
 *
 *
 * Now Your JWT Auth Flow Is Complete!
 * Here's the full chain:
 * /api/auth/login → generates token
 * Frontend sends token in Authorization: Bearer ...
 * JwtTokenFilter intercepts request
 * Validates token, extracts roles, sets Authentication
 * Secured endpoints work with @PreAuthorize, role checks, etc.
 *
 * *  Next Optional Steps (Let me know if/when)
 * * Create a test controller (/api/secure/hello) to verify token auth
 * * Add @PreAuthorize("hasRole('ROLE_ADMIN')") demo
 * * Switch to PostgreSQL + Docker (instead of in-memory H2)
 * * Start file upload storage for music (Box-like)
 *
 * Would you like to test the JWT login and try a secure endpoint next, or move to PostgreSQL/Docker setup?
 *
 */


@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found: " + username));

        var roles =
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName()))
                        .collect(Collectors.toSet());
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                roles
        );
    }
}
