package org.example.kpiservice.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Login request with email and password")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailPasswordLoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Schema(description = "Employee email", example = "sanoth.debnath@exabyting.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Schema(description = "Employee password", example = "SecurePass123")
    private String password;
}
