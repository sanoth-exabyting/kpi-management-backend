package org.example.kpiservice.service;

import org.example.kpiservice.dtos.response.EmployeeResponse;
import org.example.kpiservice.dtos.response.UserProfileResponse;

import java.util.List;
import java.util.Map;

public interface EmployeeService {
    List<EmployeeResponse> getEmployeesByTeamLead(List<Long> teamIds, String currentUserId);

    UserProfileResponse getCurrentUserProfile(String userEmail,
                                              java.util.List<java.util.Map<String, Object>> teams);
}
