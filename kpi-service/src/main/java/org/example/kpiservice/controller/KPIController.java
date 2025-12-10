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
import org.example.kpiservice.dtos.request.CreateKPIRequest;
import org.example.kpiservice.dtos.request.UpdateKPIRequest;
import org.example.kpiservice.dtos.response.KPIResponse;
import org.example.kpiservice.service.KPIService;
import org.example.kpiservice.util.JwtHelper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "KPIs", description = "KPI management endpoints")
@RestController
@RequestMapping("/api/v1/kpis")
@RequiredArgsConstructor
public class KPIController {

        private final KPIService kpiService;
        private final JwtHelper jwtHelper;

        @Operation(summary = "Create KPI", description = "Create a new KPI. Only team LEADs can create KPIs for their teams.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "201", description = "KPI created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = KPIResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input", content = @Content),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid token", content = @Content),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Not a team LEAD", content = @Content)
        })
        @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<ApiResponseEntity<KPIResponse, Void>> createKPI(
                        @Valid @RequestBody CreateKPIRequest request,
                        HttpServletRequest httpRequest) {

                // Get authenticated user's email
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String userEmail = authentication.getName();

                // Extract teams from JWT
                List<Map<String, Object>> teams = jwtHelper.extractTeams(httpRequest);

                // Create KPI
                KPIResponse response = kpiService.createKPI(request, userEmail, teams);

                return ApiResponseEntity.created(response);
        }

        @Operation(summary = "Get User KPIs", description = "Get list of ACTIVE KPIs for teams the authenticated user belongs to", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved KPIs", content = @Content(mediaType = "application/json", schema = @Schema(implementation = KPIResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid token", content = @Content)
        })
        @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<ApiResponseEntity<List<KPIResponse>, Void>> getUserKPIs(HttpServletRequest httpRequest) {
                // Extract teams from JWT
                List<Map<String, Object>> teams = jwtHelper.extractTeams(httpRequest);

                // Get authenticated user's email
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String userEmail = authentication.getName();

                // Get user's ACTIVE KPIs
                List<KPIResponse> kpis = kpiService.getUserKPIs(userEmail, teams);

                return ApiResponseEntity.ok(kpis);
        }

        @Operation(summary = "Get KPI by ID", description = "Get a specific KPI by ID. User must belong to the KPI's team.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved KPI", content = @Content(mediaType = "application/json", schema = @Schema(implementation = KPIResponse.class))),
                        @ApiResponse(responseCode = "404", description = "KPI not found", content = @Content),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Not a member of KPI's team", content = @Content),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid token", content = @Content)
        })
        @GetMapping(value = "/{kpiId}", produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<ApiResponseEntity<KPIResponse, Void>> getKPIById(
                        @PathVariable Long kpiId,
                        HttpServletRequest httpRequest) {

                // Extract teams from JWT
                List<Map<String, Object>> teams = jwtHelper.extractTeams(httpRequest);

                // Get authenticated user's email
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String userEmail = authentication.getName();

                // Get KPI with team authorization check
                KPIResponse kpi = kpiService.getKPIById(kpiId, userEmail, teams);

                return ApiResponseEntity.ok(kpi);
        }

        @Operation(summary = "Update KPI", description = "Update KPI details. Only team LEADs can update KPIs.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "KPI updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = KPIResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input", content = @Content),
                        @ApiResponse(responseCode = "404", description = "KPI not found", content = @Content),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Not a team LEAD", content = @Content),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid token", content = @Content)
        })
        @PatchMapping(value = "/{kpiId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<ApiResponseEntity<KPIResponse, Void>> updateKPI(
                        @PathVariable Long kpiId,
                        @RequestBody UpdateKPIRequest request,
                        HttpServletRequest httpRequest) {

                // Get authenticated user's email
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String userEmail = authentication.getName();

                // Extract teams from JWT
                List<Map<String, Object>> teams = jwtHelper.extractTeams(httpRequest);

                // Update KPI
                KPIResponse response = kpiService.updateKPI(kpiId, request, userEmail, teams);

                return ApiResponseEntity.ok(response);
        }

        @Operation(summary = "Delete KPI", description = "Soft delete a KPI. Only team LEADs can delete KPIs.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "KPI deleted successfully", content = @Content),
                        @ApiResponse(responseCode = "404", description = "KPI not found", content = @Content),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Not a team LEAD", content = @Content),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid token", content = @Content)
        })
        @DeleteMapping("/{kpiId}")
        public ResponseEntity<ApiResponseEntity<Void, Void>> deleteKPI(
                        @PathVariable Long kpiId,
                        HttpServletRequest httpRequest) {

                // Get authenticated user's email
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String userEmail = authentication.getName();

                List<Map<String, Object>> teams = jwtHelper.extractTeams(httpRequest);

                kpiService.deleteKPI(kpiId, userEmail, teams);

                return ApiResponseEntity.ok(null);
        }
}
