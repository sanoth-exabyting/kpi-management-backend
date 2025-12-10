package org.example.kpiservice.util;

import org.example.kpiservice.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class TeamAccessValidator {

    public void checkTeamMembership(Long kpiTeamId, List<Map<String, Object>> teams) {
        List<Long> userTeamIds = teams.stream()
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

        if (!userTeamIds.contains(kpiTeamId)) {
            throw ApiException.create(HttpStatus.FORBIDDEN,
                    "You do not have access to this KPI");
        }
    }

    public void checkLeadRole(Long kpiTeamId, List<Map<String, Object>> teams) {
        boolean isLead = teams.stream()
                .anyMatch(team -> {
                    Object teamId = team.get("team_id");
                    Object role = team.get("role");
                    Long teamIdLong = teamId instanceof Integer ? ((Integer) teamId).longValue()
                            : (Long) teamId;
                    return teamIdLong.equals(kpiTeamId) && "LEAD".equals(role);
                });

        if (!isLead) {
            throw ApiException.create(HttpStatus.FORBIDDEN,
                    "You must be a LEAD of team " + kpiTeamId + " to perform this action");
        }
    }
}
