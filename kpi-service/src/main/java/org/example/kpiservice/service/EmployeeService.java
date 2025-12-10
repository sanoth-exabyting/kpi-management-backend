package org.example.kpiservice.service;

import org.example.kpiservice.dtos.response.EmployeeResponse;

import java.util.List;

public interface EmployeeService {
    List<EmployeeResponse> getEmployeesByTeamLead(List<Long> teamIds, String currentUserId);
}
