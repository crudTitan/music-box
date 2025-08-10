package com.boxclone.musicbox.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.util.Map;

/**
 * This is the middleware that:
 * Extracts JWT from the Authorization header
 * Validates it using JwtTokenProvider
 * Sets the Authentication into the Spring Security context
 */

@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private static final Logger log = LoggerFactory.getLogger(JwtTokenFilter.class);
    private final ObjectMapper objectMapper = new ObjectMapper();


    private void writeErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        Map<String, String> body = Map.of("error", message);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = null;
        String requestUrl = "";
        try {
            requestUrl =  getRequestUrlQuery(request);
            token = jwtTokenProvider.resolveToken(request);

            log.debug("Enter, doFilter url:{}", requestUrl);

            if (token != null && jwtTokenProvider.validateToken(token)) {
                Authentication auth = jwtTokenProvider.getAuthentication(token);
                if (auth instanceof AbstractAuthenticationToken tokenWithDetails) {
                    tokenWithDetails.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                }
                SecurityContextHolder.getContext().setAuthentication(auth);
                traceAuth( token, auth );
            }

            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            log.warn("Expired token:{} url:{} warn:{}", token, requestUrl, e.getMessage());
            writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
            SecurityContextHolder.clearContext();
            return;
        } catch (IllegalArgumentException e) {
            log.warn("Unauthorized token:{} url:{} warn:{}", token,requestUrl, e.getMessage());
            writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            SecurityContextHolder.clearContext();
            return;
        } catch (JwtException e) {
            log.warn("JWT error, token:{} url:{} warn:{}", token, requestUrl, e.getMessage());
            writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: " + e.getMessage());
            SecurityContextHolder.clearContext();
            return;
        }
    }

    private void traceAuth(String token, Authentication auth) {
        try {
            log.debug("Authentication set in context: {}", auth);
            log.debug("Principal: {}", auth.getPrincipal());
            log.debug("Authorities: {}", auth.getAuthorities());
            log.debug("Details: {}", auth.getDetails());
            log.debug("Jwt token: {}", token);
        } catch (Exception e) {
            log.warn("Failed to log authentication: {}", e.getMessage());
        }
    }

    private String getRequestUrlQuery(HttpServletRequest request) {
        String requestUrl = request.getRequestURL().toString();
        String queryString = request.getQueryString();
        return  queryString != null ? requestUrl + "?" + queryString : requestUrl;
    }

}
