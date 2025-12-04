package org.example.kpiservice.service;

import org.example.kpiservice.dtos.response.GoogleResponse;

import java.util.Optional;

/**
 * Service for handling external OAuth provider validations
 */
public interface OAuthProviderService {

    /**
     * Validate Google OAuth token
     *
     * @param token Google OAuth access token
     * @return Optional containing GoogleResponse with user email
     */
    Optional<GoogleResponse> loginToGoogle(String token);
}
