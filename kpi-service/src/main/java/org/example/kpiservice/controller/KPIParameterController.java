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
import org.example.kpiservice.dtos.request.CreateProgressRequest;
import org.example.kpiservice.dtos.request.UpdateKPIParameterRequest;
import org.example.kpiservice.dtos.response.EmployeeKPIProgressResponse;
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
            @Valid @RequestBody CreateKPIParameterRequest request) {

        // Get authenticated user's email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        // Create KPI parameter
        KPIParameterResponse response = kpiParameterService.createKPIParameter(kpiId, request, userEmail);

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
            @PathVariable Long kpiId) {

        // Get authenticated user's email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        // Get KPI parameters
        List<KPIParameterResponse> parameters = kpiParameterService.getKPIParameters(kpiId, userEmail);

        return ApiResponseEntity.ok(parameters);
    }

    @Operation(summary = "Get KPI Parameter by ID", description = "Get a specific KPI parameter by ID. User must be a member of the KPI's team.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved parameter", content = @Content(mediaType = "application/json", schema = @Schema(implementation = KPIParameterResponse.class))),
            @ApiResponse(responseCode = "404", description = "KPI or parameter not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Parameter does not belong to KPI", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not a team member", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid token", content = @Content)
    })
    @GetMapping(value = "/{parameterId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseEntity<KPIParameterResponse, Void>> getKPIParameterById(
            @PathVariable Long kpiId,
            @PathVariable Long parameterId) {

        // Get authenticated user's email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        // Get KPI parameter
        KPIParameterResponse parameter = kpiParameterService.getKPIParameterById(kpiId, parameterId, userEmail);

        return ApiResponseEntity.ok(parameter);
    }

    @Operation(summary = "Update KPI Parameter", description = "Update KPI parameter details. Only team LEADs can update parameters.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "Parameter updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = KPIParameterResponse.class))),
            @ApiResponse(responseCode = "404", description = "KPI or parameter not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Parameter does not belong to KPI", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not a team LEAD", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid token", content = @Content)
    })
    @PatchMapping(value = "/{parameterId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseEntity<KPIParameterResponse, Void>> updateKPIParameter(
            @PathVariable Long kpiId,
            @PathVariable Long parameterId,
            @RequestBody UpdateKPIParameterRequest request) {

        // Get authenticated user's email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        // Update KPI parameter
        KPIParameterResponse response = kpiParameterService.updateKPIParameter(kpiId, parameterId, request, userEmail);

        return ApiResponseEntity.ok(response);
    }

    @Operation(summary = "Delete KPI Parameter", description = "Delete a KPI parameter. Only team LEADs can delete parameters.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "Parameter deleted successfully", content = @Content),
            @ApiResponse(responseCode = "404", description = "KPI or parameter not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Parameter does not belong to KPI", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not a team LEAD", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid token", content = @Content)
    })
    @DeleteMapping("/{parameterId}")
    public ResponseEntity<ApiResponseEntity<Void, Void>> deleteKPIParameter(
            @PathVariable Long kpiId,
            @PathVariable Long parameterId) {

        // Get authenticated user's email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        // Delete KPI parameter
        kpiParameterService.deleteKPIParameter(kpiId, parameterId, userEmail);

        return ApiResponseEntity.ok(null);
    }

    @Operation(summary = "Create Employee Progress", description = "Create employee progress for a KPI parameter. User must be a member of the KPI's team. Each employee can only create progress once per parameter.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @ApiResponse(responseCode = "201", description = "Progress created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmployeeKPIProgressResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflict - Progress already exists for this employee and parameter", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input or parameter doesn't belong to KPI", content = @Content),
            @ApiResponse(responseCode = "404", description = "KPI or parameter not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not a team member", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid token", content = @Content)
    })
    @PostMapping(value = "/{parameterId}/progress", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseEntity<EmployeeKPIProgressResponse, Void>> createProgress(
            @PathVariable Long kpiId,
            @PathVariable Long parameterId,
            @Valid @RequestBody CreateProgressRequest request,
            HttpServletRequest httpRequest) {

        // Get authenticated user's email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        // Extract teams from JWT
        List<Map<String, Object>> teams = jwtHelper.extractTeams(httpRequest);

        // Create progress
        EmployeeKPIProgressResponse response = kpiParameterService.createProgress(kpiId, parameterId, request,
                userEmail, teams);

        return ApiResponseEntity.created(response);
    }
}
