package org.example.kpiservice.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.kpiservice.enums.KPIStatus;

import java.time.ZonedDateTime;

@Schema(description = "Request to update KPI details")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateKPIRequest {

    @Schema(description = "KPI name", example = "Updated Sales Target Q1")
    private String name;

    @Schema(description = "KPI description", example = "Updated description")
    private String description;

    @Schema(description = "KPI start date with timezone", example = "2024-01-01T00:00:00+06:00")
    private ZonedDateTime startAt;

    @Schema(description = "KPI end date with timezone", example = "2024-03-31T23:59:59+06:00")
    private ZonedDateTime endAt;

    @Schema(description = "KPI status", example = "ACTIVE")
    private KPIStatus status;
}
