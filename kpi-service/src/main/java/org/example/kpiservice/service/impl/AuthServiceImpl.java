package org.example.kpiservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kpiservice.dtos.request.EmailPasswordLoginRequest;
import org.example.kpiservice.dtos.request.LoginRequest;
import org.example.kpiservice.dtos.request.RegisterRequest;
import org.example.kpiservice.dtos.response.GoogleResponse;
import org.example.kpiservice.dtos.response.LoginResponse;
import org.example.kpiservice.dtos.response.RegisterResponse;
import org.example.kpiservice.enums.EmployeeStatus;
import org.example.kpiservice.exception.ApiException;
import org.example.kpiservice.repository.EmployeeRepository;
import org.example.kpiservice.secondary.repository.SecondaryEmployeeRepository;
import org.example.kpiservice.security.JwtUtil;
import org.example.kpiservice.service.AuthService;
import org.example.kpiservice.service.OAuthProviderService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SecondaryEmployeeRepository secondaryEmployeeRepository;
    private final EmployeeRepository primaryEmployeeRepository;
    private final OAuthProviderService oAuthProviderService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final org.example.kpiservice.secondary.repository.TeamMemberRepository teamMemberRepository;

    @Override
    public Optional<LoginResponse> login(LoginRequest request) {
        return getGoogleLoginResponse(request);
    }

    @Override
    public LoginResponse loginWithEmailPassword(EmailPasswordLoginRequest request) {
        String email = request.getEmail();

        // Find employee in primary DB
        org.example.kpiservice.entity.Employee employee = primaryEmployeeRepository
                .findByEmail(email)
                .orElseThrow(() -> ApiException.create(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), employee.getPassword())) {
            throw ApiException.create(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        // Check employee status
        if (employee.getStatus() == EmployeeStatus.TERMINATED) {
            throw ApiException.create(HttpStatus.FORBIDDEN, "Your account has been terminated. Please contact HR.");
        }

        // Get all team memberships from TeamMember table in secondary DB
        List<Map<String, Object>> teams = teamMemberRepository.findAllByEmployeeId(employee.getEmployeeId())
                .stream()
                .map(teamMember -> {
                    Map<String, Object> team = new HashMap<>();
                    team.put("team_id", teamMember.getTeamId());
                    team.put("role", teamMember.getRole().name());
                    return team;
                })
                .toList();

        // Generate JWT token with teams
        String token = jwtUtil.generateToken(employee.getEmail(), employee.getEmployeeId(), teams);

        log.info("User logged in successfully: {} with {} team(s)", email, teams.size());

        return LoginResponse.builder()
                .accessToken(token)
                .employeeId(employee.getEmployeeId())
                .name(employee.getName())
                .email(employee.getEmail())
                .build();
    }

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String email = request.getEmail();

        // Check if email exists in secondary DB (employee database)
        Optional<org.example.kpiservice.secondary.entity.Employee> secondaryEmployeeOpt = secondaryEmployeeRepository
                .findByEmail(email);

        if (secondaryEmployeeOpt.isEmpty()) {
            throw ApiException.badRequest("Email not found in employee records. Please contact HR.");
        }

        // Check if user already registered in primary DB
        Optional<org.example.kpiservice.entity.Employee> existingEmployee = primaryEmployeeRepository
                .findByEmail(email);

        if (existingEmployee.isPresent()) {
            throw ApiException.badRequest("User already registered. Please login instead.");
        }

        // Get employee data from secondary DB
        org.example.kpiservice.secondary.entity.Employee secondaryEmployee = secondaryEmployeeOpt.get();

        // Check employee status
        if (convertStatus(secondaryEmployee.getStatus()) == EmployeeStatus.TERMINATED) {
            throw ApiException.create(HttpStatus.FORBIDDEN, "Your account has been terminated. Please contact HR.");
        }

        // Hash the password
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // Create a new employee in primary DB with data from secondary DB
        org.example.kpiservice.entity.Employee newEmployee = org.example.kpiservice.entity.Employee.builder()
                .email(secondaryEmployee.getEmail())
                .password(hashedPassword)
                .employeeId(secondaryEmployee.getEmployeeId())
                .name(secondaryEmployee.getName())
                .partnerId(secondaryEmployee.getPartnerId())
                .supervisorId(secondaryEmployee.getSupervisorId())
                .dateOfBirth(convertToInstant(secondaryEmployee.getDateOfBirth()))
                .joiningDate(convertToInstant(secondaryEmployee.getJoiningDate()))
                .designation(secondaryEmployee.getDesignation())
                .gender(convertGender(secondaryEmployee.getGender()))
                .nid(secondaryEmployee.getNid())
                .tinNumber(secondaryEmployee.getTinNumber())
                .bloodGroup(convertBloodGroup(secondaryEmployee.getBloodGroup()))
                .status(convertStatus(secondaryEmployee.getStatus()))
                .build();

        // Save first time (audit fields will be null initially)
        org.example.kpiservice.entity.Employee savedEmployee = primaryEmployeeRepository.save(newEmployee);

        // Update audit fields to self-reference
        savedEmployee.setCreatedBy(savedEmployee.getId());
        savedEmployee.setUpdatedBy(savedEmployee.getId());
        savedEmployee = primaryEmployeeRepository.save(savedEmployee);

        log.info("Successfully registered employee: {} with ID: {}", savedEmployee.getEmail(), savedEmployee.getId());

        return RegisterResponse.builder()
                .id(savedEmployee.getId())
                .employeeId(savedEmployee.getEmployeeId())
                .email(savedEmployee.getEmail())
                .name(savedEmployee.getName())
                .message("Registration successful! You can now login with your credentials.")
                .build();
    }

    private Instant convertToInstant(java.time.ZonedDateTime zonedDateTime) {
        return zonedDateTime != null ? zonedDateTime.toInstant() : null;
    }

    private org.example.kpiservice.enums.Gender convertGender(org.example.kpiservice.secondary.enums.Gender gender) {
        return gender != null ? org.example.kpiservice.enums.Gender.valueOf(gender.name()) : null;
    }

    private org.example.kpiservice.enums.BloodGroup convertBloodGroup(
            org.example.kpiservice.secondary.enums.BloodGroup bloodGroup) {
        return bloodGroup != null ? org.example.kpiservice.enums.BloodGroup.valueOf(bloodGroup.name()) : null;
    }

    private EmployeeStatus convertStatus(org.example.kpiservice.secondary.enums.EmployeeStatus status) {
        return status != null ? EmployeeStatus.valueOf(status.name()) : null;
    }

    private Optional<LoginResponse> getGoogleLoginResponse(LoginRequest loginRequest) {
        Optional<GoogleResponse> googleResponse = oAuthProviderService.loginToGoogle(loginRequest.getToken());
        if (googleResponse.isEmpty() || googleResponse.get().getEmail() == null) {
            throw ApiException.badRequest("Token is invalid");
        }
        return validateAuthentication(googleResponse.get().getEmail());
    }

    private Optional<LoginResponse> validateAuthentication(String email) {
        validateExabytingEmployee(email);
        Optional<org.example.kpiservice.secondary.entity.Employee> employeeOptional = secondaryEmployeeRepository
                .findByEmailAndStatusNot(email,
                        org.example.kpiservice.secondary.enums.EmployeeStatus.TERMINATED);
        if (employeeOptional.isPresent()) {
            org.example.kpiservice.secondary.entity.Employee employee = employeeOptional.get();
            String token = jwtUtil.generateToken(employee.getEmail(), employee.getEmployeeId());
            return Optional.of(LoginResponse.builder()
                    .accessToken(token)
                    .employeeId(employee.getEmployeeId())
                    .name(employee.getName())
                    .email(employee.getEmail())
                    .build());
        } else {
            throw ApiException.notFound("We can't find you in our employee's list, please contact with admin.");
        }
    }

    private void validateExabytingEmployee(String email) {
        if (!email.endsWith("@exabyting.com")) {
            throw ApiException.create(HttpStatus.UNAUTHORIZED, "Unauthorized email");
        }
    }
}
