package com.boxclone.musicbox.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;


@Service
public class SpotifyAuthService {

        @Value("${spotify.client-id}")
        private String clientId;

        @Value("${spotify.client-secret}")
        private String clientSecret;

        @Value("${spotify.redirect-uri}")
        private String redirectUri;

        @Autowired
        private JwtService jwtService; // your existing JWT service

        public String exchangeCodeForToken(String code) {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            String basicAuth = Base64.getEncoder()
                    .encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));
            headers.set("Authorization", "Basic " + basicAuth);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "authorization_code");
            body.add("code", code);
            body.add("redirect_uri", redirectUri);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            Map<String, Object> response = restTemplate.postForObject(
                    "https://accounts.spotify.com/api/token",
                    request,
                    Map.class
            );

            if (response == null || !response.containsKey("access_token")) {
                throw new RuntimeException("Failed to get access token from Spotify");
            }

            String accessToken = (String) response.get("access_token");
            String refreshToken = (String) response.get("refresh_token");
            Integer expiresIn = (Integer) response.get("expires_in");

            // Optionally: fetch user info from Spotify
            Map<String, Object> spotifyUser = getSpotifyUser(accessToken);

            // Generate your app JWT including Spotify user ID or email
            String jwtToken = jwtService.generateToken(spotifyUser.get("id").toString());

            // TODO: save Spotify tokens to DB if you want to refresh later

            return jwtToken;
        }

        private Map<String, Object> getSpotifyUser(String accessToken) {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://api.spotify.com/v1/me",
                    HttpMethod.GET,
                    request,
                    Map.class
            );

            return response.getBody();
        }

}
