package org.example.kpiservice.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Request to update KPI parameter")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateKPIParameterRequest {

    @Schema(description = "Parameter name", example = "Updated Revenue Amount")
    private String name;

    @Schema(description = "Parameter description", example = "Updated target revenue in USD")
    private String description;

    @Schema(description = "Target value", example = "100")
    @jakarta.validation.constraints.Min(0)
    @jakarta.validation.constraints.Max(100)
    private Integer targetValue;

    @Schema(description = "Is required", example = "true")
    private Boolean isRequired;
}
