package org.example.kpiservice.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Schema(description = "Team information response")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamResponse {

    @Schema(description = "Team ID", example = "1")
    private Long id;

    @Schema(description = "Team name", example = "Alpha Team")
    private String name;

    @Schema(description = "Team description", example = "Development team for project Alpha")
    private String description;

    @Schema(description = "Team status", example = "ACTIVE")
    private String status;

    @Schema(description = "Team creation date", example = "2024-01-15T10:30:00Z")
    private ZonedDateTime createdOn;
}
