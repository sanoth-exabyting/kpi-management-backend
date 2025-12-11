package org.example.kpiservice.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Schema(description = "KPI parameter response")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KPIParameterResponse {

    @Schema(description = "Parameter ID", example = "1")
    private Long id;

    @Schema(description = "KPI ID", example = "1")
    private Long kpiId;

    @Schema(description = "Parameter name", example = "Revenue Amount")
    private String name;

    @Schema(description = "Parameter description", example = "Target revenue in USD")
    private String description;

    @Schema(description = "Target value", example = "100")
    private Integer targetValue;

    @Schema(description = "Is required", example = "true")
    private Boolean isRequired;

    @Schema(description = "Created by employee ID", example = "123")
    private Long createdBy;

    @Schema(description = "Created at timestamp", example = "2024-12-09T12:00:00Z")
    private Instant createdAt;

    @Schema(description = "Updated by employee ID", example = "123")
    private Long updatedBy;

    @Schema(description = "Updated at timestamp", example = "2024-12-09T12:00:00Z")
    private Instant updatedAt;
}
