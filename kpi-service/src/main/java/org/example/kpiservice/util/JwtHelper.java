package org.example.kpiservice.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.kpiservice.security.JwtUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtHelper {

    private final JwtUtil jwtUtil;

    public String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    public List<Map<String, Object>> extractTeams(HttpServletRequest request) {
        String token = extractToken(request);
        return token != null ? jwtUtil.extractTeams(token) : List.of();
    }

    public List<Long> extractLeadTeamIds(HttpServletRequest request) {
        List<Map<String, Object>> teams = extractTeams(request);
        return teams.stream()
                .filter(team -> "LEAD".equals(team.get("role")))
                .map(team -> {
                    Object teamId = team.get("team_id");
                    if (teamId instanceof Integer) {
                        return ((Integer) teamId).longValue();
                    } else if (teamId instanceof Long) {
                        return (Long) teamId;
                    }
                    return null;
                })
                .filter(id -> id != null)
                .toList();
    }
}
