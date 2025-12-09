package org.example.kpiservice.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Schema(description = "Employee KPI parameter progress response")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeKPIProgressResponse {

    @Schema(description = "Progress ID", example = "1")
    private Long id;

    @Schema(description = "KPI parameter ID", example = "5")
    private Long kpiParameterId;

    @Schema(description = "Employee ID", example = "123")
    private Long employeeId;

    @Schema(description = "Progress value (0-100)", example = "75")
    private Integer progressValue;

    @Schema(description = "Notes", example = "Completed initial phase")
    private String notes;

    @Schema(description = "Comment", example = "On track for completion")
    private String comment;

    @Schema(description = "Created by employee ID", example = "123")
    private Long createdBy;

    @Schema(description = "Created at timestamp", example = "2024-12-09T12:00:00Z")
    private Instant createdAt;

    @Schema(description = "Updated by employee ID", example = "123")
    private Long updatedBy;

    @Schema(description = "Updated at timestamp", example = "2024-12-09T12:00:00Z")
    private Instant updatedAt;
}
