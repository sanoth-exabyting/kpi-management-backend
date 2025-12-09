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
}
