package org.example.kpiservice.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Login response with JWT token and employee details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    @Schema(description = "JWT access token for API authentication", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "Employee ID", example = "EMP001")
    private String employeeId;

    @Schema(description = "Employee full name", example = "Sanoth Debnath")
    private String name;

    @Schema(description = "Employee email", example = "sanoth.debnath@exabyting.com")
    private String email;
}
