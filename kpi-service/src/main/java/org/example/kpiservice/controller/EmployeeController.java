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
import org.example.kpiservice.dtos.response.EmployeeResponse;
import org.example.kpiservice.service.EmployeeService;
import org.example.kpiservice.util.JwtHelper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import org.example.kpiservice.repository.EmployeeRepository;
import org.example.kpiservice.security.JwtUtil;

@Tag(name = "Employees", description = "Employee management endpoints")
@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final JwtHelper jwtHelper;
    private final JwtUtil jwtUtil;
    private final EmployeeRepository employeeRepository;

    @Operation(summary = "Get Employees by Team Lead", description = "Get list of employees who are members of teams where the current user is a Team Lead", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved employees", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmployeeResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token", content = @Content)
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseEntity<List<EmployeeResponse>, Void>> getEmployees(HttpServletRequest request) {
        // Extract teams from JWT
        List<Map<String, Object>> teams = jwtHelper.extractTeams(request);

        // Filter teams where user is LEAD and extract team IDs
        List<Long> leadTeamIds = teams.stream()
                .filter(team -> "LEAD".equals(team.get("role")))
                .map(team -> {
                    Object teamId = team.get("team_id");
                    if (teamId instanceof Integer) {
                        return ((Integer) teamId).longValue();
                    } else if (teamId instanceof Long) {
                        return (Long) teamId;
                    }
                    return null;
                })
                .filter(id -> id != null)
                .toList();

        // Extract current user ID from token
        String token = jwtHelper.extractToken(request);
        String currentUserEmail = jwtUtil.extractUsername(token);
        String currentUserId = employeeRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> org.example.kpiservice.exception.ApiException
                        .create(org.springframework.http.HttpStatus.UNAUTHORIZED, "User not found"))
                .getEmployeeId();

        // Get employees from service
        List<EmployeeResponse> employees = employeeService.getEmployeesByTeamLead(leadTeamIds, currentUserId);

        return ApiResponseEntity.ok(employees);
    }
}
