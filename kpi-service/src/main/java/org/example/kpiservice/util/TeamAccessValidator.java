package org.example.kpiservice.util;

import org.example.kpiservice.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import org.example.kpiservice.entity.Employee;
import org.example.kpiservice.repository.EmployeeRepository;
import org.example.kpiservice.secondary.entity.TeamMember;
import org.example.kpiservice.secondary.repository.TeamMemberRepository;

import java.util.List;
import java.util.Map;

@Component
public class TeamAccessValidator {

    private final EmployeeRepository employeeRepository;
    private final TeamMemberRepository teamMemberRepository;

    public TeamAccessValidator(EmployeeRepository employeeRepository, TeamMemberRepository teamMemberRepository) {
        this.employeeRepository = employeeRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    public void validateAccess(String kpiOwnerId, String currentUserEmail, List<Map<String, Object>> teams) {
        Employee currentUser = employeeRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> ApiException.create(HttpStatus.UNAUTHORIZED, "Employee not found"));

        // Allow if user is the owner
        if (currentUser.getEmployeeId().equals(kpiOwnerId)) {
            return;
        }

        // Check if user is LEAD of any team the KPI owner is in
        List<TeamMember> targetEmployeeTeams = teamMemberRepository.findAllByEmployeeId(kpiOwnerId);

        boolean isLeadOfTarget = teams.stream()
                .anyMatch(userTeam -> {
                    Object userTeamIdObj = userTeam.get("team_id");
                    Object role = userTeam.get("role");
                    Long userTeamId = userTeamIdObj instanceof Integer ? ((Integer) userTeamIdObj).longValue()
                            : (Long) userTeamIdObj;

                    return "LEAD".equals(role) && targetEmployeeTeams.stream()
                            .anyMatch(targetTeam -> targetTeam.getTeamId().equals(userTeamId));
                });

        if (!isLeadOfTarget) {
            throw ApiException.create(HttpStatus.FORBIDDEN,
                    "You do not have access to this KPI");
        }
    }
}
