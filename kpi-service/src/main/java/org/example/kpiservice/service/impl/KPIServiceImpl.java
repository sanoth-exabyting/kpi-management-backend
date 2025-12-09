package org.example.kpiservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kpiservice.dtos.request.CreateKPIRequest;
import org.example.kpiservice.dtos.request.UpdateKPIRequest;
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

    @Override
    public KPIResponse getKPIById(Long kpiId, List<Map<String, Object>> teams) {
        // Find KPI by ID
        KPI kpi = kpiRepository.findById(kpiId)
                .orElseThrow(() -> ApiException.create(HttpStatus.NOT_FOUND, "KPI not found"));

        // Extract team IDs from user's teams
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

        // Check if KPI's team_id is in user's teams
        if (!userTeamIds.contains(kpi.getTeamId())) {
            throw ApiException.create(HttpStatus.FORBIDDEN,
                    "You do not have access to this KPI");
        }

        log.info("User accessed KPI: {} from team: {}", kpiId, kpi.getTeamId());

        return mapToResponse(kpi);
    }

    @Override
    @Transactional
    public KPIResponse updateKPI(Long kpiId, UpdateKPIRequest request, String userEmail,
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
                    "You must be a LEAD of team " + kpi.getTeamId() + " to update this KPI");
        }

        // Update only allowed fields (null values are ignored for partial updates)
        if (request.getName() != null) {
            kpi.setName(request.getName());
        }
        if (request.getDescription() != null) {
            kpi.setDescription(request.getDescription());
        }
        if (request.getStartAt() != null) {
            // Validate start_at < end_at if both are provided or if only start_at is
            // updated
            java.time.Instant newStartAt = request.getStartAt().toInstant();
            java.time.Instant endAt = request.getEndAt() != null ? request.getEndAt().toInstant() : kpi.getEndAt();

            if (!newStartAt.isBefore(endAt)) {
                throw ApiException.badRequest("Start date must be before end date");
            }
            kpi.setStartAt(newStartAt);
        }
        if (request.getEndAt() != null) {
            // Validate start_at < end_at
            java.time.Instant startAt = request.getStartAt() != null ? request.getStartAt().toInstant()
                    : kpi.getStartAt();
            java.time.Instant newEndAt = request.getEndAt().toInstant();

            if (!startAt.isBefore(newEndAt)) {
                throw ApiException.badRequest("Start date must be before end date");
            }
            kpi.setEndAt(newEndAt);
        }

        // Update audit fields (updated_by and updated_at)
        // Note: created_by and created_at remain unchanged
        kpi.setUpdatedBy(employee.getId());
        // updated_at will be automatically set by @PreUpdate in BaseEntity

        // Save updated KPI
        KPI updatedKPI = kpiRepository.save(kpi);

        log.info("KPI updated: {} by employee: {}", kpiId, employee.getEmployeeId());

        return mapToResponse(updatedKPI);
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
