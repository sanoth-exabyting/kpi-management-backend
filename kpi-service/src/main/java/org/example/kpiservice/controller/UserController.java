package org.example.kpiservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.kpiservice.dtos.ApiResponseEntity;
import org.example.kpiservice.dtos.response.UserProfileResponse;
import org.example.kpiservice.service.EmployeeService;
import org.example.kpiservice.util.JwtHelper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "User", description = "Current user profile endpoints")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {

    private final EmployeeService employeeService;
    private final JwtHelper jwtHelper;
    private final org.example.kpiservice.security.JwtUtil jwtUtil;
    private final org.example.kpiservice.service.KPIService kpiService;

    @Operation(summary = "Get Current User Profile", description = "Get current authenticated user's profile with team information", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user profile", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserProfileResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token", content = @Content)
    })
    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseEntity<UserProfileResponse, Void>> getCurrentUser(HttpServletRequest request) {
        // Get authenticated user's email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        // Extract teams from JWT
        List<Map<String, Object>> teams = jwtHelper.extractTeams(request);

        // Get user profile
        UserProfileResponse profile = employeeService.getCurrentUserProfile(userEmail, teams);

        return ApiResponseEntity.ok(profile);
    }

    @Operation(summary = "Get Current User's KPIs", description = "Get list of KPIs assigned to the current user, ordered by end date (most recent first)", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved KPIs", content = @Content(mediaType = "application/json", schema = @Schema(implementation = org.example.kpiservice.dtos.response.KPIResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token", content = @Content)
    })
    @GetMapping(value = "/me/kpis", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseEntity<List<org.example.kpiservice.dtos.response.KPIResponse>, Void>> getCurrentUserKPIs(
            HttpServletRequest request) {
        // Extract employee ID from JWT token
        String token = jwtHelper.extractToken(request);
        String employeeId = jwtUtil.extractEmployeeId(token);

        // Get user's KPIs via KPIService
        List<org.example.kpiservice.dtos.response.KPIResponse> kpis = kpiService.getCurrentUserKPIs(employeeId);

        return ApiResponseEntity.ok(kpis);
    }

    @Operation(summary = "Get Current User's KPI by ID", description = "Get details of a specific KPI assigned to the current user", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved KPI", content = @Content(mediaType = "application/json", schema = @Schema(implementation = org.example.kpiservice.dtos.response.KPIResponse.class))),
            @ApiResponse(responseCode = "404", description = "KPI not found or not assigned to user", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token", content = @Content)
    })
    @GetMapping(value = "/me/kpis/{kpiId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseEntity<org.example.kpiservice.dtos.response.KPIResponse, Void>> getCurrentUserKPIById(
            @org.springframework.web.bind.annotation.PathVariable Long kpiId,
            HttpServletRequest request) {
        // Extract employee ID from JWT token
        String token = jwtHelper.extractToken(request);
        String employeeId = jwtUtil.extractEmployeeId(token);

        // Get specific KPI details
        org.example.kpiservice.dtos.response.KPIResponse kpi = kpiService.getCurrentUserKPIById(kpiId, employeeId);

        return ApiResponseEntity.ok(kpi);
    }

    @Operation(summary = "Get Current User's KPI Parameters", description = "Get list of parameters for a specific KPI assigned to the current user", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved KPI parameters", content = @Content(mediaType = "application/json", schema = @Schema(implementation = org.example.kpiservice.dtos.response.KPIParameterResponse.class))),
            @ApiResponse(responseCode = "404", description = "KPI not found or not assigned to user", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token", content = @Content)
    })
    @GetMapping(value = "/me/kpis/{kpiId}/parameters", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseEntity<List<org.example.kpiservice.dtos.response.KPIParameterResponse>, Void>> getCurrentUserKPIParameters(
            @org.springframework.web.bind.annotation.PathVariable Long kpiId,
            HttpServletRequest request) {
        // Extract employee ID from JWT token
        String token = jwtHelper.extractToken(request);
        String employeeId = jwtUtil.extractEmployeeId(token);

        // Get KPI parameters
        List<org.example.kpiservice.dtos.response.KPIParameterResponse> parameters = kpiService
                .getCurrentUserKPIParameters(kpiId, employeeId);

        return ApiResponseEntity.ok(parameters);
    }

    @Operation(summary = "Get Current User's KPI Parameter by ID", description = "Get details of a specific parameter for a KPI assigned to the current user", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved parameter", content = @Content(mediaType = "application/json", schema = @Schema(implementation = org.example.kpiservice.dtos.response.KPIParameterResponse.class))),
            @ApiResponse(responseCode = "404", description = "KPI not found, not assigned to user, or parameter not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Parameter does not belong to KPI", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token", content = @Content)
    })
    @GetMapping(value = "/me/kpis/{kpiId}/parameters/{parameterId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseEntity<org.example.kpiservice.dtos.response.KPIParameterResponse, Void>> getCurrentUserKPIParameterById(
            @org.springframework.web.bind.annotation.PathVariable Long kpiId,
            @org.springframework.web.bind.annotation.PathVariable Long parameterId,
            HttpServletRequest request) {
        // Extract employee ID from JWT token
        String token = jwtHelper.extractToken(request);
        String employeeId = jwtUtil.extractEmployeeId(token);

        // Get specific parameter details
        org.example.kpiservice.dtos.response.KPIParameterResponse parameter = kpiService
                .getCurrentUserKPIParameterById(kpiId, parameterId, employeeId);

        return ApiResponseEntity.ok(parameter);
    }

    @Operation(summary = "Create Progress for KPI Parameter", description = "Create progress for a specific parameter of a KPI assigned to the current user", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @ApiResponse(responseCode = "201", description = "Progress created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = org.example.kpiservice.dtos.response.EmployeeKPIProgressResponse.class))),
            @ApiResponse(responseCode = "409", description = "Progress already exists for this parameter", content = @Content),
            @ApiResponse(responseCode = "404", description = "KPI not found, not assigned to user, or parameter not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Parameter does not belong to KPI or invalid input", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token", content = @Content)
    })
    @org.springframework.web.bind.annotation.PostMapping(value = "/me/kpis/{kpiId}/parameters/{parameterId}/progresses", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseEntity<org.example.kpiservice.dtos.response.EmployeeKPIProgressResponse, Void>> createCurrentUserProgress(
            @org.springframework.web.bind.annotation.PathVariable Long kpiId,
            @org.springframework.web.bind.annotation.PathVariable Long parameterId,
            @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody org.example.kpiservice.dtos.request.CreateProgressRequest request,
            HttpServletRequest httpRequest) {
        // Extract employee ID from JWT token
        String token = jwtHelper.extractToken(httpRequest);
        String employeeId = jwtUtil.extractEmployeeId(token);

        // Create progress
        org.example.kpiservice.dtos.response.EmployeeKPIProgressResponse progress = kpiService
                .createCurrentUserProgress(kpiId, parameterId, request, employeeId);

        return ApiResponseEntity.created(progress);
    }

    @Operation(summary = "Get Current User's Progress for Parameter", description = "Get progress record for a specific KPI parameter assigned to the current user", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved progress", content = @Content(mediaType = "application/json", schema = @Schema(implementation = org.example.kpiservice.dtos.response.EmployeeKPIProgressResponse.class))),
            @ApiResponse(responseCode = "404", description = "KPI not found, not assigned to user, parameter not found, or progress not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Parameter does not belong to KPI", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token", content = @Content)
    })
    @GetMapping(value = "/me/kpis/{kpiId}/parameters/{parameterId}/progresses", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseEntity<org.example.kpiservice.dtos.response.EmployeeKPIProgressResponse, Void>> getCurrentUserProgress(
            @org.springframework.web.bind.annotation.PathVariable Long kpiId,
            @org.springframework.web.bind.annotation.PathVariable Long parameterId,
            HttpServletRequest httpRequest) {
        // Extract employee ID from JWT token
        String token = jwtHelper.extractToken(httpRequest);
        String employeeId = jwtUtil.extractEmployeeId(token);

        // Get progress details
        org.example.kpiservice.dtos.response.EmployeeKPIProgressResponse progress = kpiService
                .getCurrentUserProgress(kpiId, parameterId, employeeId);

        return ApiResponseEntity.ok(progress);
    }

    @Operation(summary = "Update Progress for KPI Parameter", description = "Update progress for a specific KPI parameter assigned to the current user", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "Progress updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = org.example.kpiservice.dtos.response.EmployeeKPIProgressResponse.class))),
            @ApiResponse(responseCode = "404", description = "KPI not found, not assigned to user, parameter not found, or progress not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Not authorized to update this progress", content = @Content),
            @ApiResponse(responseCode = "400", description = "Parameter does not belong to KPI or invalid input", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token", content = @Content)
    })
    @org.springframework.web.bind.annotation.PatchMapping(value = "/me/kpis/{kpiId}/parameters/{parameterId}/progresses", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseEntity<org.example.kpiservice.dtos.response.EmployeeKPIProgressResponse, Void>> updateCurrentUserProgress(
            @org.springframework.web.bind.annotation.PathVariable Long kpiId,
            @org.springframework.web.bind.annotation.PathVariable Long parameterId,
            @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody org.example.kpiservice.dtos.request.CreateProgressRequest request,
            HttpServletRequest httpRequest) {
        // Extract employee ID from JWT token
        String token = jwtHelper.extractToken(httpRequest);
        String employeeId = jwtUtil.extractEmployeeId(token);

        // Update progress
        org.example.kpiservice.dtos.response.EmployeeKPIProgressResponse progress = kpiService
                .updateCurrentUserProgress(kpiId, parameterId, request, employeeId);

        return ApiResponseEntity.ok(progress);
    }

    @Operation(summary = "Get All Progresses for KPI", description = "Get all progress records for a specific KPI assigned to the current user", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved progresses", content = @Content(mediaType = "application/json", schema = @Schema(implementation = org.example.kpiservice.dtos.response.EmployeeKPIProgressResponse.class))),
            @ApiResponse(responseCode = "404", description = "KPI not found or not assigned to user", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token", content = @Content)
    })
    @GetMapping(value = "/me/kpis/{kpiId}/progresses", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseEntity<List<org.example.kpiservice.dtos.response.EmployeeKPIProgressResponse>, Void>> getCurrentUserKPIProgresses(
            @org.springframework.web.bind.annotation.PathVariable Long kpiId,
            HttpServletRequest httpRequest) {
        // Extract employee ID from JWT token
        String token = jwtHelper.extractToken(httpRequest);
        String employeeId = jwtUtil.extractEmployeeId(token);

        // Get all progresses for this KPI
        List<org.example.kpiservice.dtos.response.EmployeeKPIProgressResponse> progresses = kpiService
                .getCurrentUserKPIProgresses(kpiId, employeeId);

        return ApiResponseEntity.ok(progresses);
    }
}
