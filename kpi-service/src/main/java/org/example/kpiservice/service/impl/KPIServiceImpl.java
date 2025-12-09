package org.example.kpiservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kpiservice.dtos.request.CreateKPIRequest;
import org.example.kpiservice.dtos.response.KPIResponse;
import org.example.kpiservice.entity.Employee;
import org.example.kpiservice.entity.KPI;
import org.example.kpiservice.enums.KPIStatus;
import org.example.kpiservice.exception.ApiException;
import org.example.kpiservice.repository.EmployeeRepository;
import org.example.kpiservice.repository.KPIRepository;
import org.example.kpiservice.service.KPIService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KPIServiceImpl implements KPIService {

    private final KPIRepository kpiRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public KPIResponse createKPI(CreateKPIRequest request, String userEmail, List<Map<String, Object>> teams) {
        // Get employee by email
        Employee employee = employeeRepository.findByEmail(userEmail)
                .orElseThrow(() -> ApiException.create(HttpStatus.UNAUTHORIZED, "Employee not found"));

        // Validate user is LEAD of the specified team
        boolean isLead = teams.stream()
                .anyMatch(team -> {
                    Object teamId = team.get("team_id");
                    Object role = team.get("role");
                    Long teamIdLong = teamId instanceof Integer ? ((Integer) teamId).longValue() : (Long) teamId;
                    return teamIdLong.equals(request.getTeamId()) && "LEAD".equals(role);
                });

        if (!isLead) {
            throw ApiException.create(HttpStatus.FORBIDDEN,
                    "You must be a LEAD of team " + request.getTeamId() + " to create KPIs");
        }

        // Validate status is DRAFT or ACTIVE
        if (request.getStatus() != KPIStatus.DRAFT && request.getStatus() != KPIStatus.ACTIVE) {
            throw ApiException.badRequest("Status must be DRAFT or ACTIVE when creating KPI");
        }

        // Validate start_at < end_at
        if (!request.getStartAt().isBefore(request.getEndAt())) {
            throw ApiException.badRequest("Start date must be before end date");
        }

        // Convert ZonedDateTime to Instant (UTC) for storage
        java.time.Instant startAtUtc = request.getStartAt().toInstant();
        java.time.Instant endAtUtc = request.getEndAt().toInstant();

        // Create KPI entity
        KPI kpi = KPI.builder()
                .teamId(request.getTeamId())
                .name(request.getName())
                .description(request.getDescription())
                .startAt(startAtUtc)
                .endAt(endAtUtc)
                .status(request.getStatus())
                .isDeleted(false)
                .build();

        // Set audit fields (will be set by @PrePersist in BaseEntity, but we set
        // createdBy/updatedBy explicitly)
        kpi.setCreatedBy(employee.getId());
        kpi.setUpdatedBy(employee.getId());

        // Save KPI
        KPI savedKPI = kpiRepository.save(kpi);

        log.info("KPI created successfully: {} by employee: {}", savedKPI.getId(), employee.getEmployeeId());

        // Map to response
        return mapToResponse(savedKPI);
    }

    @Override
    public List<KPIResponse> getUserKPIs(List<Map<String, Object>> teams) {
        // Extract team IDs from teams array
        List<Long> teamIds = teams.stream()
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

        // If no teams, return empty list
        if (teamIds.isEmpty()) {
            return List.of();
        }

        // Get ACTIVE KPIs for user's teams
        List<KPI> kpis = kpiRepository.findAllByTeamIdInAndStatus(teamIds, KPIStatus.ACTIVE);

        log.info("Retrieved {} ACTIVE KPIs for user's {} team(s)", kpis.size(), teamIds.size());

        // Map to response
        return kpis.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private KPIResponse mapToResponse(KPI kpi) {
        return KPIResponse.builder()
                .id(kpi.getId())
                .teamId(kpi.getTeamId())
                .name(kpi.getName())
                .description(kpi.getDescription())
                .startAt(kpi.getStartAt())
                .endAt(kpi.getEndAt())
                .status(kpi.getStatus() != null ? kpi.getStatus().name() : null)
                .createdBy(kpi.getCreatedBy())
                .createdAt(kpi.getCreatedAt())
                .updatedBy(kpi.getUpdatedBy())
                .updatedAt(kpi.getUpdatedAt())
                .build();
    }
}
