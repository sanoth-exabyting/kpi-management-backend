package org.example.kpiservice.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Schema(description = "KPI information response")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KPIResponse {

    @Schema(description = "KPI ID", example = "1")
    private Long id;

    @Schema(description = "Employee ID", example = "EMP001")
    private String employeeId;

    @Schema(description = "KPI name", example = "Sales Target Q1")
    private String name;

    @Schema(description = "KPI description", example = "Achieve 1M revenue in Q1")
    private String description;

    @Schema(description = "Start date", example = "2024-01-01T00:00:00Z")
    private Instant startAt;

    @Schema(description = "End date", example = "2024-03-31T23:59:59Z")
    private Instant endAt;

    @Schema(description = "KPI status", example = "DRAFT")
    private String status;

    @Schema(description = "Created by employee ID", example = "EMP001")
    private Long createdBy;

    @Schema(description = "Created at timestamp", example = "2024-12-09T12:00:00Z")
    private Instant createdAt;

    @Schema(description = "Updated by employee ID", example = "EMP001")
    private Long updatedBy;

    @Schema(description = "Updated at timestamp", example = "2024-12-09T12:00:00Z")
    private Instant updatedAt;
}
