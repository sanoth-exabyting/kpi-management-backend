package org.example.kpiservice.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.kpiservice.enums.LoginType;

@Schema(description = "Login request with Google OAuth token")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @Schema(description = "Google OAuth access token", example = "ya29.a0AfH6SMBx...")
    private String token;

    @Schema(description = "Login type", example = "GOOGLE")
    private LoginType loginType;
}
