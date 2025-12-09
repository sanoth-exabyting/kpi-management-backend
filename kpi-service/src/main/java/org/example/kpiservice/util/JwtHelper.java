package org.example.kpiservice.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.kpiservice.security.JwtUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Helper utility for JWT-related operations in controllers
 */
@Component
@RequiredArgsConstructor
public class JwtHelper {

    private final JwtUtil jwtUtil;

    /**
     * Extract JWT token from Authorization header
     *
     * @param request HTTP request
     * @return JWT token or null if not present
     */
    public String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * Extract teams from JWT token in request
     *
     * @param request HTTP request
     * @return List of teams from JWT token, or empty list if no token
     */
    public List<Map<String, Object>> extractTeams(HttpServletRequest request) {
        String token = extractToken(request);
        return token != null ? jwtUtil.extractTeams(token) : List.of();
    }
}
