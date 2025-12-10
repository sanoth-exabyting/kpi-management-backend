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
import org.example.kpiservice.secondary.entity.TeamMember;
import org.example.kpiservice.secondary.repository.TeamMemberRepository;
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
    private final TeamMemberRepository teamMemberRepository;

    @Override
    @Transactional
    public KPIResponse createKPI(CreateKPIRequest request, String userEmail, List<Map<String, Object>> teams) {
        // Get employee by email
        Employee employee = employeeRepository.findByEmail(userEmail)
                .orElseThrow(() -> ApiException.create(HttpStatus.UNAUTHORIZED, "Employee not found"));

        // Authorization: Allow if creating for self OR if user is LEAD of a team the
        // target employee is in
        if (!employee.getEmployeeId().equals(request.getEmployeeId())) {
            // Check if user is LEAD of any team the target employee is in
            List<TeamMember> targetEmployeeTeams = teamMemberRepository.findAllByEmployeeId(request.getEmployeeId());

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
                        "Only Lead of the target employee's team can create a KPI for them");
            }
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
                .employeeId(request.getEmployeeId())
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
    public List<KPIResponse> getUserKPIs(String userEmail, List<Map<String, Object>> teams) {
        // Get employee by email
        Employee employee = employeeRepository.findByEmail(userEmail)
                .orElseThrow(() -> ApiException.create(HttpStatus.UNAUTHORIZED, "Employee not found"));

        // Get ACTIVE KPIs for the user (where employeeId matches)
        List<KPI> kpis = kpiRepository.findAllByEmployeeIdAndStatus(employee.getEmployeeId(), KPIStatus.ACTIVE);

        log.info("Retrieved {} ACTIVE KPIs for user: {}", kpis.size(), employee.getEmployeeId());

        // Map to response
        return kpis.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public KPIResponse getKPIById(Long kpiId, String userEmail, List<Map<String, Object>> teams) {
        // Get employee by email
        Employee employee = employeeRepository.findByEmail(userEmail)
                .orElseThrow(() -> ApiException.create(HttpStatus.UNAUTHORIZED, "Employee not found"));

        // Find KPI by ID
        KPI kpi = kpiRepository.findById(kpiId)
                .orElseThrow(() -> ApiException.create(HttpStatus.NOT_FOUND, "KPI not found"));

        // Check if user has access to this KPI
        // Access allowed if:
        // 1. User is the owner (employeeId matches)
        // 2. User is LEAD of a team the KPI's employee belongs to

        if (!employee.getEmployeeId().equals(kpi.getEmployeeId())) {
            // Check if user is LEAD of any team the KPI's employee is in
            List<TeamMember> targetEmployeeTeams = teamMemberRepository.findAllByEmployeeId(kpi.getEmployeeId());

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

        log.info("User accessed KPI: {}", kpiId);

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

        // Authorization: Allow if updating self OR if user is LEAD of a team the target
        // employee is in
        if (!employee.getEmployeeId().equals(kpi.getEmployeeId())) {
            // Check if user is LEAD of any team the KPI's employee is in
            List<TeamMember> targetEmployeeTeams = teamMemberRepository.findAllByEmployeeId(kpi.getEmployeeId());

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
                        "You must be a LEAD of a team the employee belongs to, or update your own KPI");
            }
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
        if (request.getStatus() != null) {
            kpi.setStatus(request.getStatus());
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

    @Override
    @Transactional
    public void deleteKPI(Long kpiId, String userEmail, List<Map<String, Object>> teams) {
        // Get employee by email
        Employee employee = employeeRepository.findByEmail(userEmail)
                .orElseThrow(() -> ApiException.create(HttpStatus.UNAUTHORIZED, "Employee not found"));

        // Find KPI by ID
        KPI kpi = kpiRepository.findById(kpiId)
                .orElseThrow(() -> ApiException.create(HttpStatus.NOT_FOUND, "KPI not found"));

        // Authorization: Allow if deleting self OR if user is LEAD of a team the target
        // employee is in
        if (!employee.getEmployeeId().equals(kpi.getEmployeeId())) {
            // Check if user is LEAD of any team the KPI's employee is in
            List<TeamMember> targetEmployeeTeams = teamMemberRepository.findAllByEmployeeId(kpi.getEmployeeId());

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
                        "You must be a LEAD of a team the employee belongs to, or delete your own KPI");
            }
        }

        // Soft delete (triggers @SQLDelete which sets is_deleted = true)
        kpiRepository.delete(kpi);

        log.info("KPI soft deleted: {} by employee: {}", kpiId, employee.getEmployeeId());
    }

    private KPIResponse mapToResponse(KPI kpi) {
        return KPIResponse.builder()
                .id(kpi.getId())
                .employeeId(kpi.getEmployeeId())
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
