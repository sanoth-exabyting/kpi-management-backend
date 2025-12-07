package org.example.kpiservice.service;

import org.example.kpiservice.dtos.request.EmailPasswordLoginRequest;
import org.example.kpiservice.dtos.request.LoginRequest;
import org.example.kpiservice.dtos.request.RegisterRequest;
import org.example.kpiservice.dtos.response.LoginResponse;
import org.example.kpiservice.dtos.response.RegisterResponse;

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

    /**
     * Authenticate user with email and password
     *
     * @param request Login request containing email and password
     * @return LoginResponse with JWT token and employee details
     */
    LoginResponse loginWithEmailPassword(EmailPasswordLoginRequest request);

    /**
     * Register a new employee with email and password
     *
     * @param request Registration request containing email and password
     * @return RegisterResponse with employee details and success message
     */
    RegisterResponse register(RegisterRequest request);
}
