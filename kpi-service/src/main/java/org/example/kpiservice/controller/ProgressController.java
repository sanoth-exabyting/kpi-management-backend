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
import org.example.kpiservice.dtos.request.UpdateProgressRequest;
import org.example.kpiservice.dtos.response.EmployeeKPIProgressResponse;
import org.example.kpiservice.service.KPIParameterService;
import org.example.kpiservice.util.JwtHelper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

@Tag(name = "Progress", description = "Employee KPI progress management endpoints")
@RestController
@RequestMapping("/api/v1/kpis")
@RequiredArgsConstructor
public class ProgressController {

        private final KPIParameterService kpiParameterService;
        private final JwtHelper jwtHelper;

        @Operation(summary = "Get User's KPI Progress", description = "Get all progress records created by the current user for a specific KPI. User must be a member of the KPI's team.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved progress", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmployeeKPIProgressResponse.class))),
                        @ApiResponse(responseCode = "404", description = "KPI not found", content = @Content),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Not a team member", content = @Content),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid token", content = @Content)
        })
        @GetMapping(value = "/{kpiId}/progress", produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<ApiResponseEntity<List<EmployeeKPIProgressResponse>, Void>> getUserKPIProgress(
                        @PathVariable Long kpiId,
                        HttpServletRequest httpRequest) {

                // Get authenticated user's email
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String userEmail = authentication.getName();

                // Extract teams from JWT
                List<Map<String, Object>> teams = jwtHelper.extractTeams(httpRequest);

                // Get user's progress for the KPI
                List<EmployeeKPIProgressResponse> progressList = kpiParameterService.getUserKPIProgress(kpiId,
                                userEmail,
                                teams);

                return ApiResponseEntity.ok(progressList);
        }

        @Operation(summary = "Get Specific Progress Details", description = "Get details of a specific progress record created by the current user for a specific KPI. User must be a member of the KPI's team.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved progress details", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmployeeKPIProgressResponse.class))),
                        @ApiResponse(responseCode = "404", description = "KPI or Progress not found", content = @Content),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Not a team member or not owner of progress", content = @Content),
                        @ApiResponse(responseCode = "400", description = "Bad Request - Progress does not belong to KPI", content = @Content),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid token", content = @Content)
        })
        @GetMapping(value = "/{kpiId}/progress/{progressId}", produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<ApiResponseEntity<EmployeeKPIProgressResponse, Void>> getKPIProgressById(
                        @PathVariable Long kpiId,
                        @PathVariable Long progressId,
                        HttpServletRequest httpRequest) {

                // Get authenticated user's email
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String userEmail = authentication.getName();

                // Extract teams from JWT
                List<Map<String, Object>> teams = jwtHelper.extractTeams(httpRequest);

                // Get specific progress details
                EmployeeKPIProgressResponse response = kpiParameterService.getKPIProgressById(kpiId, progressId,
                                userEmail, teams);

                return ApiResponseEntity.ok(response);
        }

        @Operation(summary = "Update Employee Progress", description = "Update specific progress record. Only progressValue, notes, and comment can be updated. User must be the owner of the progress record.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "Progress updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmployeeKPIProgressResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input or progress doesn't belong to KPI", content = @Content),
                        @ApiResponse(responseCode = "404", description = "KPI or Progress not found", content = @Content),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Not a team member or not owner of progress", content = @Content),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid token", content = @Content)
        })
        @PatchMapping(value = "/{kpiId}/progress/{progressId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<ApiResponseEntity<EmployeeKPIProgressResponse, Void>> updateKPIProgress(
                        @PathVariable Long kpiId,
                        @PathVariable Long progressId,
                        @Valid @RequestBody UpdateProgressRequest request,
                        HttpServletRequest httpRequest) {

                // Get authenticated user's email
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String userEmail = authentication.getName();

                // Extract teams from JWT
                List<Map<String, Object>> teams = jwtHelper.extractTeams(httpRequest);

                // Update progress
                EmployeeKPIProgressResponse response = kpiParameterService.updateKPIProgress(kpiId, progressId, request,
                                userEmail, teams);

                return ApiResponseEntity.ok(response);
        }
}
