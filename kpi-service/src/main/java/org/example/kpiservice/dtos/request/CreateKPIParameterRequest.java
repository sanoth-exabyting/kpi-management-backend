package org.example.kpiservice.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Request to create KPI parameter")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateKPIParameterRequest {

    @NotBlank(message = "Name is required")
    @Schema(description = "Parameter name", example = "Revenue Amount")
    private String name;

    @Schema(description = "Parameter description", example = "Target revenue in USD")
    private String description;

    @Schema(description = "Target value", example = "100", defaultValue = "0")
    @jakarta.validation.constraints.Min(0)
    @jakarta.validation.constraints.Max(100)
    @Builder.Default
    private Integer targetValue = 0;

    @Schema(description = "Is required", example = "true", defaultValue = "true")
    @Builder.Default
    private Boolean isRequired = true;
}
