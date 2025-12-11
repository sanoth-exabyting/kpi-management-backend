package org.example.kpiservice.service;

import org.example.kpiservice.dtos.response.GoogleResponse;

import java.util.Optional;


public interface OAuthProviderService {
    Optional<GoogleResponse> loginToGoogle(String token);
}
