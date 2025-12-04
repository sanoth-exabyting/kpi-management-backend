package org.example.kpiservice.service;

import org.example.kpiservice.dtos.request.LoginRequest;
import org.example.kpiservice.dtos.response.LoginResponse;

import java.util.Optional;

/**
 * Service for handling authentication operations
 */
public interface AuthService {

    /**
     * Authenticate user with Google OAuth token
     *
     * @param request Login request containing Google OAuth token
     * @return Optional containing LoginResponse with JWT token and employee details
     */
    Optional<LoginResponse> login(LoginRequest request);
}
