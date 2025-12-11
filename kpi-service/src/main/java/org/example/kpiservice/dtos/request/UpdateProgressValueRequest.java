package org.example.kpiservice.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Request to update progress value")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProgressValueRequest {

    @NotNull(message = "Progress value is required")
    @Min(value = 0, message = "Progress value must be at least 0")
    @Max(value = 100, message = "Progress value must not exceed 100")
    @Schema(description = "Progress value (0-100)", example = "85")
    private Integer progressValue;
}
