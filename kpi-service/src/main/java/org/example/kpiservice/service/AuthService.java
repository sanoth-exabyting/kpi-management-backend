package org.example.kpiservice.service;

import org.example.kpiservice.dtos.request.EmailPasswordLoginRequest;
import org.example.kpiservice.dtos.request.LoginRequest;
import org.example.kpiservice.dtos.request.RegisterRequest;
import org.example.kpiservice.dtos.response.LoginResponse;
import org.example.kpiservice.dtos.response.RegisterResponse;

import java.util.Optional;


public interface AuthService {

    Optional<LoginResponse> login(LoginRequest request);

    LoginResponse loginWithEmailPassword(EmailPasswordLoginRequest request);

    RegisterResponse register(RegisterRequest request);
}
