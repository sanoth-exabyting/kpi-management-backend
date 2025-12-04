package org.example.kpiservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kpiservice.dtos.request.LoginRequest;
import org.example.kpiservice.dtos.response.GoogleResponse;
import org.example.kpiservice.dtos.response.LoginResponse;
import org.example.kpiservice.exception.RequestValidationException;
import org.example.kpiservice.secondary.entity.Employee;
import org.example.kpiservice.secondary.enums.EmployeeStatus;
import org.example.kpiservice.secondary.repository.EmployeeRepository;
import org.example.kpiservice.security.JwtUtil;
import org.example.kpiservice.service.AuthService;
import org.example.kpiservice.service.OAuthProviderService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final EmployeeRepository employeeRepository;
    private final OAuthProviderService oAuthProviderService;
    private final JwtUtil jwtUtil;

    @Override
    public Optional<LoginResponse> login(LoginRequest request) {
        return getGoogleLoginResponse(request);
    }

    private Optional<LoginResponse> getGoogleLoginResponse(LoginRequest loginRequest) {
        Optional<GoogleResponse> googleResponse = oAuthProviderService.loginToGoogle(loginRequest.getToken());
        if (googleResponse.isEmpty() || googleResponse.get().getEmail() == null) {
            throw RequestValidationException.from("Token is invalid");
        }
        return validateAuthentication(googleResponse.get().getEmail());
    }

    private Optional<LoginResponse> validateAuthentication(String email) {
        validateExabytingEmployee(email);
        Optional<Employee> employeeOptional = employeeRepository.findByEmailAndStatusNot(email,
                EmployeeStatus.TERMINATED);
        if (employeeOptional.isPresent()) {
            Employee employee = employeeOptional.get();
            String token = jwtUtil.generateToken(employee.getEmail());
            return Optional.of(LoginResponse.builder()
                    .accessToken(token)
                    .employeeId(employee.getEmployeeId())
                    .name(employee.getName())
                    .email(employee.getEmail())
                    .build());
        } else {
            throw RequestValidationException.from(HttpStatus.NOT_FOUND,
                    "We can't find you in our employee's list, please contact with admin.");
        }
    }

    private void validateExabytingEmployee(String email) {
        if (!email.endsWith("@exabyting.com")) {
            RequestValidationException.throwFrom(HttpStatus.UNAUTHORIZED, "Unauthorized email");
        }
    }
}
