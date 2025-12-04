package org.example.kpiservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kpiservice.dtos.response.GoogleResponse;
import org.example.kpiservice.service.OAuthProviderService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthProviderServiceImpl implements OAuthProviderService {

    private final RestTemplate restTemplate;

    @Override
    public Optional<GoogleResponse> loginToGoogle(String token) {
        try {
            String url = "https://www.googleapis.com/oauth2/v3/tokeninfo?access_token=" + token;
            GoogleResponse response = restTemplate.getForObject(url, GoogleResponse.class);
            return Optional.ofNullable(response);
        } catch (Exception e) {
            log.error("Error validating Google token: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
