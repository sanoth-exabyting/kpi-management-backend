package org.example.kpiservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kpiservice.dtos.request.CreateKPIParameterRequest;
import org.example.kpiservice.dtos.response.KPIParameterResponse;
import org.example.kpiservice.entity.Employee;
import org.example.kpiservice.entity.KPI;
import org.example.kpiservice.entity.KPIParameter;
import org.example.kpiservice.exception.ApiException;
import org.example.kpiservice.repository.EmployeeRepository;
import org.example.kpiservice.repository.KPIParameterRepository;
import org.example.kpiservice.repository.KPIRepository;
import org.example.kpiservice.service.KPIParameterService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KPIParameterServiceImpl implements KPIParameterService {

    private final KPIParameterRepository kpiParameterRepository;
    private final KPIRepository kpiRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public KPIParameterResponse createKPIParameter(Long kpiId, CreateKPIParameterRequest request, String userEmail,
            List<Map<String, Object>> teams) {
        // Get employee by email
        Employee employee = employeeRepository.findByEmail(userEmail)
                .orElseThrow(() -> ApiException.create(HttpStatus.UNAUTHORIZED, "Employee not found"));

        // Find KPI by ID
        KPI kpi = kpiRepository.findById(kpiId)
                .orElseThrow(() -> ApiException.create(HttpStatus.NOT_FOUND, "KPI not found"));

        // Check if user is LEAD of the KPI's team
        boolean isLead = teams.stream()
                .anyMatch(team -> {
                    Object teamId = team.get("team_id");
                    Object role = team.get("role");
                    Long teamIdLong = teamId instanceof Integer ? ((Integer) teamId).longValue() : (Long) teamId;
                    return teamIdLong.equals(kpi.getTeamId()) && "LEAD".equals(role);
                });

        if (!isLead) {
            throw ApiException.create(HttpStatus.FORBIDDEN,
                    "You must be a LEAD of team " + kpi.getTeamId() + " to create KPI parameters");
        }

        // Create KPI parameter
        KPIParameter parameter = KPIParameter.builder()
                .kpi(kpi)
                .name(request.getName())
                .description(request.getDescription())
                .build();

        // Set audit fields
        parameter.setCreatedBy(employee.getId());
        parameter.setUpdatedBy(employee.getId());

        // Save parameter
        KPIParameter savedParameter = kpiParameterRepository.save(parameter);

        log.info("KPI parameter created: {} for KPI: {} by employee: {}", savedParameter.getId(), kpiId,
                employee.getEmployeeId());

        return mapToResponse(savedParameter);
    }

    private KPIParameterResponse mapToResponse(KPIParameter parameter) {
        return KPIParameterResponse.builder()
                .id(parameter.getId())
                .kpiId(parameter.getKpi().getId())
                .name(parameter.getName())
                .description(parameter.getDescription())
                .createdBy(parameter.getCreatedBy())
                .createdAt(parameter.getCreatedAt())
                .updatedBy(parameter.getUpdatedBy())
                .updatedAt(parameter.getUpdatedAt())
                .build();
    }
}
