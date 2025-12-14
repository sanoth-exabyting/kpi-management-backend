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

                // Check if user is a team LEAD
                List<Long> leadTeamIds = jwtHelper.extractLeadTeamIds(httpRequest);
                if (leadTeamIds.isEmpty()) {
                        throw org.example.kpiservice.exception.ApiException.create(
                                        org.springframework.http.HttpStatus.FORBIDDEN,
                                        "Only team LEADs can create KPIs");
                }
                // Get authenticated user's email
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String userEmail = authentication.getName();

                // Create KPI
                KPIResponse response = kpiService.createKPI(request, userEmail);

                return ApiResponseEntity.created(response);
        }

        @Operation(summary = "Get User KPIs", description = "Get list of KPIs created by the authenticated user", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved KPIs", content = @Content(mediaType = "application/json", schema = @Schema(implementation = KPIResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid token", content = @Content)
        })
        @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<ApiResponseEntity<List<KPIResponse>, Void>> getUserKPIs() {
                // Get authenticated user's email
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String userEmail = authentication.getName();

                // Get user's ACTIVE KPIs
                List<KPIResponse> kpis = kpiService.getUserKPIs(userEmail);

                return ApiResponseEntity.ok(kpis);
        }

        @Operation(summary = "Get KPI by ID", description = "Get a specific KPI by ID. User must be the creator of the KPI.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved KPI", content = @Content(mediaType = "application/json", schema = @Schema(implementation = KPIResponse.class))),
                        @ApiResponse(responseCode = "404", description = "KPI not found", content = @Content),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Not the creator of the KPI", content = @Content),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid token", content = @Content)
        })
        @GetMapping(value = "/{kpiId}", produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<ApiResponseEntity<KPIResponse, Void>> getKPIById(
                        @PathVariable Long kpiId) {

                // Get authenticated user's email
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String userEmail = authentication.getName();

                // Get KPI with team authorization check
                KPIResponse kpi = kpiService.getKPIById(kpiId, userEmail);

                return ApiResponseEntity.ok(kpi);
        }

        @Operation(summary = "Update KPI", description = "Update KPI details. Only the creator can update KPIs.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "KPI updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = KPIResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input", content = @Content),
                        @ApiResponse(responseCode = "404", description = "KPI not found", content = @Content),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Not the creator of the KPI", content = @Content),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid token", content = @Content)
        })
        @PatchMapping(value = "/{kpiId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<ApiResponseEntity<KPIResponse, Void>> updateKPI(
                        @PathVariable Long kpiId,
                        @RequestBody UpdateKPIRequest request) {

                // Get authenticated user's email
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String userEmail = authentication.getName();

                // Update KPI
                KPIResponse response = kpiService.updateKPI(kpiId, request, userEmail);

                return ApiResponseEntity.ok(response);
        }

        @Operation(summary = "Delete KPI", description = "Soft delete a KPI. Only the creator can delete KPIs.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "KPI deleted successfully", content = @Content),
                        @ApiResponse(responseCode = "404", description = "KPI not found", content = @Content),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Not the creator of the KPI", content = @Content),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid token", content = @Content)
        })
        @DeleteMapping("/{kpiId}")
        public ResponseEntity<ApiResponseEntity<Void, Void>> deleteKPI(
                        @PathVariable Long kpiId) {

                // Get authenticated user's email
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String userEmail = authentication.getName();

                kpiService.deleteKPI(kpiId, userEmail);

                return ApiResponseEntity.ok(null);
        }

        @Operation(summary = "Assign Employees to KPI", description = "Assign team members to a KPI. Only team LEADs can assign members from their teams.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "Employees assigned successfully", content = @Content),
                        @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input", content = @Content),
                        @ApiResponse(responseCode = "404", description = "KPI not found", content = @Content),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Not a team LEAD or invalid employees", content = @Content),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid token", content = @Content)
        })
        @PostMapping(value = "/{kpiId}/assignees", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<ApiResponseEntity<Void, Void>> assignEmployeesToKPI(
                        @PathVariable Long kpiId,
                        @Valid @RequestBody org.example.kpiservice.dtos.request.AssignEmployeesToKPIRequest request,
                        HttpServletRequest httpRequest) {

                // Get authenticated user's email
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String userEmail = authentication.getName();

                kpiService.assignEmployeesToKPI(kpiId, request, userEmail, httpRequest);

                return ApiResponseEntity.ok(null);
        }

        @Operation(summary = "Get KPI Assignees", description = "Get list of employees assigned to a KPI. Only the creator can view assignees.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved assignees", content = @Content(mediaType = "application/json")),
                        @ApiResponse(responseCode = "404", description = "KPI not found", content = @Content),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Not the creator of the KPI", content = @Content),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid token", content = @Content)
        })
        @GetMapping(value = "/{kpiId}/assignees", produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<ApiResponseEntity<java.util.List<org.example.kpiservice.dtos.response.EmployeeResponse>, Void>> getKPIAssignees(
                        @PathVariable Long kpiId) {

                // Get authenticated user's email
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String userEmail = authentication.getName();

                java.util.List<org.example.kpiservice.dtos.response.EmployeeResponse> assignees = kpiService
                                .getKPIAssignees(kpiId, userEmail);

                return ApiResponseEntity.ok(assignees);
        }

        @Operation(summary = "Get KPI Assignee by ID", description = "Get details of a specific employee assigned to a KPI. Only the creator can view assignee details.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved assignee", content = @Content(mediaType = "application/json", schema = @Schema(implementation = org.example.kpiservice.dtos.response.EmployeeResponse.class))),
                        @ApiResponse(responseCode = "404", description = "KPI not found or employee not assigned", content = @Content),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Not the creator of the KPI", content = @Content),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid token", content = @Content)
        })
        @GetMapping(value = "/{kpiId}/assignees/{employeeId}", produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<ApiResponseEntity<org.example.kpiservice.dtos.response.EmployeeResponse, Void>> getKPIAssigneeById(
                        @PathVariable Long kpiId,
                        @PathVariable String employeeId) {

                // Get authenticated user's email
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String userEmail = authentication.getName();

                org.example.kpiservice.dtos.response.EmployeeResponse assignee = kpiService.getKPIAssigneeById(kpiId,
                                employeeId, userEmail);

                return ApiResponseEntity.ok(assignee);
        }

        @Operation(summary = "Remove KPI Assignee", description = "Remove an employee from a KPI assignment. Only the creator can remove assignees.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "Assignee removed successfully", content = @Content),
                        @ApiResponse(responseCode = "404", description = "KPI not found or employee not assigned", content = @Content),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Not the creator of the KPI", content = @Content),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid token", content = @Content)
        })
        @DeleteMapping(value = "/{kpiId}/assignees/{employeeId}")
        public ResponseEntity<ApiResponseEntity<Void, Void>> removeKPIAssignee(
                        @PathVariable Long kpiId,
                        @PathVariable String employeeId) {

                // Get authenticated user's email
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String userEmail = authentication.getName();

                kpiService.removeKPIAssignee(kpiId, employeeId, userEmail);

                return ApiResponseEntity.ok(null);
        }

        @Operation(summary = "Get Assignee Progress", description = "Get all progress records for a specific assignee on a KPI. Only the KPI creator can view assignee progress.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved assignee progress", content = @Content(mediaType = "application/json", schema = @Schema(implementation = org.example.kpiservice.dtos.response.EmployeeKPIProgressResponse.class))),
                        @ApiResponse(responseCode = "404", description = "KPI not found", content = @Content),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Only KPI creator can view assignee progress", content = @Content),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token", content = @Content)
        })
        @GetMapping(value = "/{kpiId}/assignees/{employeeId}/progresses", produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<ApiResponseEntity<List<org.example.kpiservice.dtos.response.EmployeeKPIProgressResponse>, Void>> getAssigneeProgresses(
                        @PathVariable Long kpiId,
                        @PathVariable String employeeId) {
                // Get authenticated user's email
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String userEmail = authentication.getName();

                // Get assignee progress
                List<org.example.kpiservice.dtos.response.EmployeeKPIProgressResponse> progresses = kpiService
                                .getAssigneeProgresses(kpiId, employeeId, userEmail);

                return ApiResponseEntity.ok(progresses);
        }

        @Operation(summary = "Get Assignee Progress by ID", description = "Get specific progress details for an assignee on a KPI. Only the KPI creator can view assignee progress.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved progress", content = @Content(mediaType = "application/json", schema = @Schema(implementation = org.example.kpiservice.dtos.response.EmployeeKPIProgressResponse.class))),
                        @ApiResponse(responseCode = "404", description = "KPI or progress not found", content = @Content),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Only KPI creator can view assignee progress", content = @Content),
                        @ApiResponse(responseCode = "400", description = "Progress does not belong to employee or KPI", content = @Content),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token", content = @Content)
        })
        @GetMapping(value = "/{id}/assignees/{employee_id}/progresses/{progressId}", produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<ApiResponseEntity<org.example.kpiservice.dtos.response.EmployeeKPIProgressResponse, Void>> getAssigneeProgressById(
                        @PathVariable Long id,
                        @PathVariable("employee_id") String employeeId,
                        @PathVariable Long progressId) {
                // Get authenticated user's email
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String userEmail = authentication.getName();

                // Get specific progress
                org.example.kpiservice.dtos.response.EmployeeKPIProgressResponse progress = kpiService
                                .getAssigneeProgressById(id, employeeId, progressId, userEmail);

                return ApiResponseEntity.ok(progress);
        }

        @Operation(summary = "Update Assignee Progress Value", description = "Update the progress value for an assignee's progress. Only the KPI creator can update assignee progress.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "Progress value updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = org.example.kpiservice.dtos.response.EmployeeKPIProgressResponse.class))),
                        @ApiResponse(responseCode = "404", description = "KPI or progress not found", content = @Content),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Only KPI creator can update assignee progress", content = @Content),
                        @ApiResponse(responseCode = "400", description = "Progress does not belong to employee or KPI, or invalid input", content = @Content),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token", content = @Content)
        })
        @org.springframework.web.bind.annotation.PatchMapping(value = "/{id}/assignees/{employee_id}/progresses/{progressId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<ApiResponseEntity<org.example.kpiservice.dtos.response.EmployeeKPIProgressResponse, Void>> updateAssigneeProgressValue(
                        @PathVariable Long id,
                        @PathVariable("employee_id") String employeeId,
                        @PathVariable Long progressId,
                        @Valid @RequestBody org.example.kpiservice.dtos.request.UpdateProgressValueRequest request) {
                // Get authenticated user's email
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String userEmail = authentication.getName();

                // Update progress value
                org.example.kpiservice.dtos.response.EmployeeKPIProgressResponse progress = kpiService
                                .updateAssigneeProgressValue(id, employeeId, progressId, request.getProgressValue(),
                                                userEmail);

                return ApiResponseEntity.ok(progress);
        }
}
