package org.example.kpiservice.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Request to update employee KPI parameter progress")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProgressRequest {

    @Min(value = 0, message = "Progress value must be at least 0")
    @Max(value = 100, message = "Progress value must not exceed 100")
    @Schema(description = "Progress value (0-100)", example = "85")
    private Integer progressValue;

    @Schema(description = "Notes about the progress", example = "Updated progress notes")
    private String notes;

    @Schema(description = "Additional comment", example = "Making good progress")
    private String comment;
}
