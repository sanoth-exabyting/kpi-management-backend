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
}
