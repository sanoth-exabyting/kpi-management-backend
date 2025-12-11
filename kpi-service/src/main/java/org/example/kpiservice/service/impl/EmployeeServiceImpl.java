package org.example.kpiservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.kpiservice.dtos.response.EmployeeResponse;
import org.example.kpiservice.secondary.entity.Employee;
import org.example.kpiservice.secondary.entity.TeamMember;
import org.example.kpiservice.secondary.repository.SecondaryEmployeeRepository;
import org.example.kpiservice.secondary.repository.TeamMemberRepository;
import org.example.kpiservice.service.EmployeeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {

        private final TeamMemberRepository teamMemberRepository;
        private final SecondaryEmployeeRepository secondaryEmployeeRepository;

        @Override
        public List<EmployeeResponse> getEmployeesByTeamLead(List<Long> teamIds, String currentUserId) {
                // 1. Get all team members for the given team IDs
                List<TeamMember> teamMembers = teamMemberRepository.findAllByTeamIdIn(teamIds);

                // 2. Extract unique employee IDs (excluding current user)
                Set<String> employeeIds = teamMembers.stream()
                                .map(TeamMember::getEmployeeId)
                                .filter(id -> !id.equals(currentUserId))
                                .collect(Collectors.toSet());

                // 3. Fetch employee details from secondary repository (excluding TERMINATED)
                List<Employee> employees = secondaryEmployeeRepository.findAllByEmployeeIdInAndStatusNot(
                                employeeIds.stream().toList(),
                                org.example.kpiservice.secondary.enums.EmployeeStatus.TERMINATED);

                // 4. Map to response DTO
                return employees.stream()
                                .map(this::mapToResponse)
                                .toList();
        }

        private EmployeeResponse mapToResponse(Employee employee) {
                return EmployeeResponse.builder()
                                .employeeId(employee.getEmployeeId())
                                .name(employee.getName())
                                .email(employee.getEmail())
                                .designation(employee.getDesignation())
                                .status(employee.getStatus())
                                .build();
        }

        @Override
        public org.example.kpiservice.dtos.response.UserProfileResponse getCurrentUserProfile(String userEmail,
                        List<Map<String, Object>> teams) {
                // Fetch employee from secondary database
                Employee employee = secondaryEmployeeRepository.findByEmail(userEmail)
                                .orElseThrow(() -> new RuntimeException("Employee not found"));

                // Map teams from JWT to team info
                List<org.example.kpiservice.dtos.response.UserProfileResponse.UserTeamInfo> teamInfoList = teams
                                .stream()
                                .map(team -> org.example.kpiservice.dtos.response.UserProfileResponse.UserTeamInfo
                                                .builder()
                                                .teamId(getLongValue(team.get("team_id")))
                                                .teamName((String) team.get("team_name"))
                                                .role((String) team.get("role"))
                                                .build())
                                .toList();

                // Build user profile response
                return org.example.kpiservice.dtos.response.UserProfileResponse.builder()
                                .employeeId(employee.getEmployeeId())
                                .name(employee.getName())
                                .email(employee.getEmail())
                                .designation(employee.getDesignation())
                                .status(employee.getStatus())
                                .teams(teamInfoList)
                                .build();
        }

        private Long getLongValue(Object value) {
                if (value instanceof Integer) {
                        return ((Integer) value).longValue();
                } else if (value instanceof Long) {
                        return (Long) value;
                }
                return null;
        }
}
