package org.example.kpiservice.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.kpiservice.enums.KPIStatus;

import java.time.ZonedDateTime;

@Schema(description = "Request to create a new KPI")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateKPIRequest {

    @NotBlank(message = "Employee ID is required")
    @Schema(description = "Employee ID", example = "EMP001")
    private String employeeId;

    @NotBlank(message = "Name is required")
    @Schema(description = "KPI name", example = "Sales Target Q1")
    private String name;

    @Schema(description = "KPI description", example = "Achieve 1M revenue in Q1")
    private String description;

    @NotNull(message = "Start date is required")
    @Schema(description = "KPI start date with timezone", example = "2024-01-01T00:00:00+06:00")
    private ZonedDateTime startAt;

    @NotNull(message = "End date is required")
    @Schema(description = "KPI end date with timezone", example = "2024-03-31T23:59:59+06:00")
    private ZonedDateTime endAt;

    @NotNull(message = "Status is required")
    @Schema(description = "KPI status (DRAFT or ACTIVE only)", example = "DRAFT")
    private KPIStatus status;
}
