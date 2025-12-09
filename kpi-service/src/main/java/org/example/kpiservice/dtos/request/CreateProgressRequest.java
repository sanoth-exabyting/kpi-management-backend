package org.example.kpiservice.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Request to create employee KPI parameter progress")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProgressRequest {

    @NotNull(message = "Progress value is required")
    @Min(value = 0, message = "Progress value must be at least 0")
    @Max(value = 100, message = "Progress value must not exceed 100")
    @Schema(description = "Progress value (0-100)", example = "75")
    private Integer progressValue;

    @Schema(description = "Notes about the progress", example = "Completed initial phase")
    private String notes;

    @Schema(description = "Additional comment", example = "On track for completion")
    private String comment;
}
