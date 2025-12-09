package org.example.kpiservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.kpiservice.dtos.ApiResponseEntity;
import org.example.kpiservice.dtos.request.CreateKPIParameterRequest;
import org.example.kpiservice.dtos.response.KPIParameterResponse;
import org.example.kpiservice.service.KPIParameterService;
import org.example.kpiservice.util.JwtHelper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "KPI Parameters", description = "KPI parameter management endpoints")
@RestController
@RequestMapping("/api/v1/kpis/{kpiId}/parameters")
@RequiredArgsConstructor
public class KPIParameterController {

    private final KPIParameterService kpiParameterService;
    private final JwtHelper jwtHelper;

    @Operation(summary = "Create KPI Parameter", description = "Create a new KPI parameter. Only team LEADs can create parameters for their team's KPIs.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @ApiResponse(responseCode = "201", description = "Parameter created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = KPIParameterResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input", content = @Content),
            @ApiResponse(responseCode = "404", description = "KPI not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not a team LEAD", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid token", content = @Content)
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseEntity<KPIParameterResponse, Void>> createKPIParameter(
            @PathVariable Long kpiId,
            @Valid @RequestBody CreateKPIParameterRequest request,
            HttpServletRequest httpRequest) {

        // Get authenticated user's email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        // Extract teams from JWT
        List<Map<String, Object>> teams = jwtHelper.extractTeams(httpRequest);

        // Create KPI parameter
        KPIParameterResponse response = kpiParameterService.createKPIParameter(kpiId, request, userEmail, teams);

        return ApiResponseEntity.created(response);
    }

    @Operation(summary = "Get KPI Parameters", description = "Get list of parameters for a specific KPI. User must be a member of the KPI's team.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved parameters", content = @Content(mediaType = "application/json", schema = @Schema(implementation = KPIParameterResponse.class))),
            @ApiResponse(responseCode = "404", description = "KPI not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not a team member", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid token", content = @Content)
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseEntity<List<KPIParameterResponse>, Void>> getKPIParameters(
            @PathVariable Long kpiId,
            HttpServletRequest httpRequest) {

        // Extract teams from JWT
        List<Map<String, Object>> teams = jwtHelper.extractTeams(httpRequest);

        // Get KPI parameters
        List<KPIParameterResponse> parameters = kpiParameterService.getKPIParameters(kpiId, teams);

        return ApiResponseEntity.ok(parameters);
    }
}
