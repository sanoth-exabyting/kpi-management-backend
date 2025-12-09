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
import org.example.kpiservice.dtos.response.TeamResponse;
import org.example.kpiservice.service.TeamService;
import org.example.kpiservice.util.JwtHelper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "Teams", description = "Team management endpoints")
@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;
    private final JwtHelper jwtHelper;

    @Operation(summary = "Get User Teams", description = "Get list of teams that the authenticated user belongs to", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved teams", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token", content = @Content)
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseEntity<List<TeamResponse>, Void>> getUserTeams(HttpServletRequest request) {
        // Extract teams from JWT
        List<Map<String, Object>> teams = jwtHelper.extractTeams(request);

        // Extract team IDs
        List<Long> teamIds = teams.stream()
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

        // Get team details from service
        List<TeamResponse> userTeams = teamService.getUserTeams(teamIds);

        return ApiResponseEntity.ok(userTeams);
    }
}
