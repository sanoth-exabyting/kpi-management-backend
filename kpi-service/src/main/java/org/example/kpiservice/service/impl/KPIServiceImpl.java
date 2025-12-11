package org.example.kpiservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kpiservice.dtos.request.AssignEmployeesToKPIRequest;
import org.example.kpiservice.dtos.request.CreateKPIRequest;
import org.example.kpiservice.dtos.request.UpdateKPIRequest;
import org.example.kpiservice.dtos.response.KPIResponse;
import org.example.kpiservice.entity.Employee;
import org.example.kpiservice.entity.KPI;
import org.example.kpiservice.enums.KPIStatus;
import org.example.kpiservice.exception.ApiException;
import org.example.kpiservice.repository.EmployeeRepository;
import org.example.kpiservice.repository.KPIRepository;
import org.example.kpiservice.secondary.repository.TeamMemberRepository;
import org.example.kpiservice.service.KPIService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KPIServiceImpl implements KPIService {

        private final KPIRepository kpiRepository;
        private final EmployeeRepository employeeRepository;
        private final TeamMemberRepository teamMemberRepository;
        private final org.example.kpiservice.repository.EmployeeKPIRepository employeeKPIRepository;
        private final org.example.kpiservice.util.JwtHelper jwtHelper;
        private final org.example.kpiservice.secondary.repository.SecondaryEmployeeRepository secondaryEmployeeRepository;
        private final org.example.kpiservice.repository.KPIParameterRepository kpiParameterRepository;
        private final org.example.kpiservice.repository.EmployeeKPIParameterRepository employeeKPIParameterRepository;

        @Override
        @Transactional
        public KPIResponse createKPI(CreateKPIRequest request, String userEmail) {
                // Get employee by email
                Employee employee = employeeRepository.findByEmail(userEmail)
                                .orElseThrow(() -> ApiException.create(HttpStatus.UNAUTHORIZED, "Employee not found"));

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

                // Create a KPI entity
                KPI kpi = KPI.builder()
                                .name(request.getName())
                                .description(request.getDescription())
                                .startAt(startAtUtc)
                                .endAt(endAtUtc)
                                .status(request.getStatus())
                                .isDeleted(false)
                                .build();

                kpi.setCreatedBy(employee.getId());
                kpi.setUpdatedBy(employee.getId());

                // Save KPI
                KPI savedKPI = kpiRepository.save(kpi);

                log.info("KPI created successfully: {} by employee: {}", savedKPI.getId(), employee.getEmployeeId());

                // Map to response
                return mapToResponse(savedKPI);
        }

        @Override
        public List<KPIResponse> getUserKPIs(String userEmail) {
                // Get employee by email
                Employee employee = employeeRepository.findByEmail(userEmail)
                                .orElseThrow(() -> ApiException.create(HttpStatus.UNAUTHORIZED, "Employee not found"));

                // Get KPIs created by the user
                List<KPI> kpis = kpiRepository.findAllByCreatedBy(employee.getId());

                log.info("Retrieved {} KPIs for user: {}", kpis.size(), employee.getEmployeeId());

                // Map to response
                return kpis.stream()
                                .map(this::mapToResponse)
                                .toList();
        }

        @Override
        public KPIResponse getKPIById(Long kpiId, String userEmail) {
                // Get employee by email
                Employee employee = employeeRepository.findByEmail(userEmail)
                                .orElseThrow(() -> ApiException.create(HttpStatus.UNAUTHORIZED, "Employee not found"));

                // Find KPI by ID
                KPI kpi = kpiRepository.findById(kpiId)
                                .orElseThrow(() -> ApiException.create(HttpStatus.NOT_FOUND, "KPI not found"));

                // Authorization: Allow if user is creator
                if (!kpi.getCreatedBy().equals(employee.getId())) {
                        throw ApiException.create(HttpStatus.FORBIDDEN, "You can only access KPIs you created");
                }

                log.info("User accessed KPI: {}", kpiId);

                return mapToResponse(kpi);
        }

        @Override
        @Transactional
        public KPIResponse updateKPI(Long kpiId, UpdateKPIRequest request, String userEmail) {
                Employee employee = employeeRepository.findByEmail(userEmail)
                                .orElseThrow(() -> ApiException.create(HttpStatus.UNAUTHORIZED, "Employee not found"));

                KPI kpi = kpiRepository.findById(kpiId)
                                .orElseThrow(() -> ApiException.create(HttpStatus.NOT_FOUND, "KPI not found"));

                // Authorization: Allow if user is creator
                if (!kpi.getCreatedBy().equals(employee.getId())) {
                        throw ApiException.create(HttpStatus.FORBIDDEN, "You can only update KPIs you created");
                }

                if (request.getName() != null) {
                        kpi.setName(request.getName());
                }
                if (request.getDescription() != null) {
                        kpi.setDescription(request.getDescription());
                }
                if (request.getStartAt() != null) {
                        java.time.Instant newStartAt = request.getStartAt().toInstant();
                        java.time.Instant endAt = request.getEndAt() != null ? request.getEndAt().toInstant()
                                        : kpi.getEndAt();

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

                kpi.setUpdatedBy(employee.getId());

                KPI updatedKPI = kpiRepository.save(kpi);

                log.info("KPI updated: {} by employee: {}", kpiId, employee.getEmployeeId());

                return mapToResponse(updatedKPI);
        }

        @Override
        @Transactional
        public void deleteKPI(Long kpiId, String userEmail) {
                Employee employee = employeeRepository.findByEmail(userEmail)
                                .orElseThrow(() -> ApiException.create(HttpStatus.UNAUTHORIZED, "Employee not found"));

                KPI kpi = kpiRepository.findById(kpiId)
                                .orElseThrow(() -> ApiException.create(HttpStatus.NOT_FOUND, "KPI not found"));

                if (!kpi.getCreatedBy().equals(employee.getId())) {
                        throw ApiException.create(HttpStatus.FORBIDDEN, "You can only delete KPIs you created");
                }

                kpiRepository.delete(kpi);

                log.info("KPI soft deleted: {} by employee: {}", kpiId, employee.getEmployeeId());
        }

        @Override
        @Transactional
        public void assignEmployeesToKPI(Long kpiId, AssignEmployeesToKPIRequest request, String userEmail,
                        jakarta.servlet.http.HttpServletRequest httpRequest) {
                Employee currentEmployee = employeeRepository.findByEmail(userEmail)
                                .orElseThrow(() -> ApiException.create(HttpStatus.UNAUTHORIZED, "Employee not found"));

                KPI kpi = kpiRepository.findById(kpiId)
                                .orElseThrow(() -> ApiException.create(HttpStatus.NOT_FOUND, "KPI not found"));

                // Get teams where current user is LEAD from JWT
                List<Long> leadTeamIds = jwtHelper.extractLeadTeamIds(httpRequest);

                if (leadTeamIds.isEmpty()) {
                        throw ApiException.create(HttpStatus.FORBIDDEN,
                                        "You must be a team LEAD to assign employees to KPIs");
                }

                // Process each employee ID
                for (String employeeId : request.getEmployeeIds()) {
                        // Skip if trying to assign to self
                        if (employeeId.equals(currentEmployee.getEmployeeId())) {
                                log.warn("Skipping self-assignment for employee: {}", employeeId);
                                continue;
                        }

                        // Check if employee is already assigned
                        if (employeeKPIRepository.existsByEmployeeIdAndKpiId(employeeId, kpiId)) {
                                log.warn("Employee {} is already assigned to KPI {}", employeeId, kpiId);
                                continue;
                        }

                        // Verify employee is a MEMBER in one of the LEAD's teams
                        List<org.example.kpiservice.secondary.entity.TeamMember> employeeTeams = teamMemberRepository
                                        .findAllByEmployeeId(employeeId);

                        boolean isAuthorized = employeeTeams.stream()
                                        .anyMatch(tm -> leadTeamIds.contains(tm.getTeamId())
                                                        && tm.getRole() == org.example.kpiservice.secondary.enums.MemberRole.MEMBER);

                        if (!isAuthorized) {
                                log.warn("Employee {} is not a MEMBER in any of the LEAD's teams", employeeId);
                                continue;
                        }

                        // Create EmployeeKPI entry
                        org.example.kpiservice.entity.EmployeeKPI employeeKPI = org.example.kpiservice.entity.EmployeeKPI
                                        .builder()
                                        .kpi(kpi)
                                        .employeeId(employeeId)
                                        .build();
                        employeeKPI.setCreatedBy(currentEmployee.getId());
                        employeeKPI.setUpdatedBy(currentEmployee.getId());
                        employeeKPIRepository.save(employeeKPI);

                        log.info("Assigned employee {} to KPI {} by LEAD {}", employeeId, kpiId,
                                        currentEmployee.getEmployeeId());
                }
        }

        @Override
        public List<org.example.kpiservice.dtos.response.EmployeeResponse> getKPIAssignees(Long kpiId,
                        String userEmail) {
                Employee currentEmployee = employeeRepository.findByEmail(userEmail)
                                .orElseThrow(() -> ApiException.create(HttpStatus.UNAUTHORIZED, "Employee not found"));

                KPI kpi = kpiRepository.findById(kpiId)
                                .orElseThrow(() -> ApiException.create(HttpStatus.NOT_FOUND, "KPI not found"));

                // Authorization: Only creator can view assignees
                if (!kpi.getCreatedBy().equals(currentEmployee.getId())) {
                        throw ApiException.create(HttpStatus.FORBIDDEN,
                                        "You can only view assignees for KPIs you created");
                }

                // Get all EmployeeKPI entries for this KPI
                List<org.example.kpiservice.entity.EmployeeKPI> employeeKPIs = employeeKPIRepository
                                .findAllByKpiId(kpiId);

                // Extract employee IDs
                List<String> employeeIds = employeeKPIs.stream()
                                .map(org.example.kpiservice.entity.EmployeeKPI::getEmployeeId)
                                .toList();

                if (employeeIds.isEmpty()) {
                        return List.of();
                }

                // Fetch employee details from secondary database
                List<org.example.kpiservice.secondary.entity.Employee> employees = secondaryEmployeeRepository
                                .findAllByEmployeeIdIn(employeeIds);

                // Map to EmployeeResponse
                return employees.stream()
                                .map(emp -> org.example.kpiservice.dtos.response.EmployeeResponse.builder()
                                                .employeeId(emp.getEmployeeId())
                                                .name(emp.getName())
                                                .email(emp.getEmail())
                                                .designation(emp.getDesignation())
                                                .status(emp.getStatus())
                                                .build())
                                .toList();
        }

        @Override
        public org.example.kpiservice.dtos.response.EmployeeResponse getKPIAssigneeById(Long kpiId, String employeeId,
                        String userEmail) {
                Employee currentEmployee = employeeRepository.findByEmail(userEmail)
                                .orElseThrow(() -> ApiException.create(HttpStatus.UNAUTHORIZED, "Employee not found"));

                KPI kpi = kpiRepository.findById(kpiId)
                                .orElseThrow(() -> ApiException.create(HttpStatus.NOT_FOUND, "KPI not found"));

                // Authorization: Only creator can view assignees
                if (!kpi.getCreatedBy().equals(currentEmployee.getId())) {
                        throw ApiException.create(HttpStatus.FORBIDDEN,
                                        "You can only view assignees for KPIs you created");
                }

                // Verify employee is assigned to this KPI
                if (!employeeKPIRepository.existsByEmployeeIdAndKpiId(employeeId, kpiId)) {
                        throw ApiException.create(HttpStatus.NOT_FOUND, "Employee is not assigned to this KPI");
                }

                // Fetch employee details from secondary database
                org.example.kpiservice.secondary.entity.Employee employee = secondaryEmployeeRepository
                                .findByEmployeeId(employeeId)
                                .orElseThrow(() -> ApiException.create(HttpStatus.NOT_FOUND, "Employee not found"));

                // Map to EmployeeResponse
                return org.example.kpiservice.dtos.response.EmployeeResponse.builder()
                                .employeeId(employee.getEmployeeId())
                                .name(employee.getName())
                                .email(employee.getEmail())
                                .designation(employee.getDesignation())
                                .status(employee.getStatus())
                                .build();
        }

        @Override
        @Transactional
        public void removeKPIAssignee(Long kpiId, String employeeId, String userEmail) {
                Employee currentEmployee = employeeRepository.findByEmail(userEmail)
                                .orElseThrow(() -> ApiException.create(HttpStatus.UNAUTHORIZED, "Employee not found"));

                KPI kpi = kpiRepository.findById(kpiId)
                                .orElseThrow(() -> ApiException.create(HttpStatus.NOT_FOUND, "KPI not found"));

                // Authorization: Only creator can remove assignees
                if (!kpi.getCreatedBy().equals(currentEmployee.getId())) {
                        throw ApiException.create(HttpStatus.FORBIDDEN,
                                        "You can only remove assignees from KPIs you created");
                }

                // Find and delete the EmployeeKPI entry
                org.example.kpiservice.entity.EmployeeKPI employeeKPI = employeeKPIRepository
                                .findByEmployeeIdAndKpiId(employeeId, kpiId)
                                .orElseThrow(() -> ApiException.create(HttpStatus.NOT_FOUND,
                                                "Employee is not assigned to this KPI"));

                employeeKPIRepository.delete(employeeKPI);

                log.info("Removed employee {} from KPI {} by creator {}", employeeId, kpiId,
                                currentEmployee.getEmployeeId());
        }

        @Override
        @Transactional(readOnly = true)
        public List<KPIResponse> getCurrentUserKPIs(String employeeId) {
                // Get all EmployeeKPI entries for this employee
                List<org.example.kpiservice.entity.EmployeeKPI> employeeKPIs = employeeKPIRepository
                                .findAllByEmployeeId(employeeId);

                // Extract KPIs and order by end_at (most recent first)
                return employeeKPIs.stream()
                                .map(org.example.kpiservice.entity.EmployeeKPI::getKpi)
                                .sorted((kpi1, kpi2) -> kpi2.getEndAt().compareTo(kpi1.getEndAt()))
                                .map(this::mapToResponse)
                                .toList();
        }

        @Override
        @Transactional(readOnly = true)
        public KPIResponse getCurrentUserKPIById(Long kpiId, String employeeId) {
                // Verify KPI is assigned to this employee
                org.example.kpiservice.entity.EmployeeKPI employeeKPI = employeeKPIRepository
                                .findByEmployeeIdAndKpiId(employeeId, kpiId)
                                .orElseThrow(() -> ApiException.create(HttpStatus.NOT_FOUND,
                                                "KPI not found or not assigned to you"));

                // Return KPI details
                return mapToResponse(employeeKPI.getKpi());
        }

        @Override
        @Transactional(readOnly = true)
        public List<org.example.kpiservice.dtos.response.KPIParameterResponse> getCurrentUserKPIParameters(Long kpiId,
                        String employeeId) {
                // Verify KPI is assigned to this employee
                if (!employeeKPIRepository.existsByEmployeeIdAndKpiId(employeeId, kpiId)) {
                        throw ApiException.create(HttpStatus.NOT_FOUND, "KPI not found or not assigned to you");
                }

                // Get all parameters for this KPI
                List<org.example.kpiservice.entity.KPIParameter> parameters = kpiParameterRepository
                                .findAllByKpiId(kpiId);

                // Map to response
                return parameters.stream()
                                .map(this::mapToParameterResponse)
                                .toList();
        }

        @Override
        @Transactional(readOnly = true)
        public org.example.kpiservice.dtos.response.KPIParameterResponse getCurrentUserKPIParameterById(Long kpiId,
                        Long parameterId, String employeeId) {
                // Verify KPI is assigned to this employee
                if (!employeeKPIRepository.existsByEmployeeIdAndKpiId(employeeId, kpiId)) {
                        throw ApiException.create(HttpStatus.NOT_FOUND, "KPI not found or not assigned to you");
                }

                // Get specific parameter
                org.example.kpiservice.entity.KPIParameter parameter = kpiParameterRepository.findById(parameterId)
                                .orElseThrow(() -> ApiException.create(HttpStatus.NOT_FOUND, "Parameter not found"));

                // Verify parameter belongs to the KPI
                if (!parameter.getKpi().getId().equals(kpiId)) {
                        throw ApiException.create(HttpStatus.BAD_REQUEST,
                                        "Parameter does not belong to the specified KPI");
                }

                // Map to response
                return mapToParameterResponse(parameter);
        }

        @Override
        @Transactional
        public org.example.kpiservice.dtos.response.EmployeeKPIProgressResponse createCurrentUserProgress(Long kpiId,
                        Long parameterId, org.example.kpiservice.dtos.request.CreateProgressRequest request,
                        String employeeId) {
                // Verify KPI is assigned to this employee
                if (!employeeKPIRepository.existsByEmployeeIdAndKpiId(employeeId, kpiId)) {
                        throw ApiException.create(HttpStatus.NOT_FOUND, "KPI not found or not assigned to you");
                }

                // Get specific parameter
                org.example.kpiservice.entity.KPIParameter parameter = kpiParameterRepository.findById(parameterId)
                                .orElseThrow(() -> ApiException.create(HttpStatus.NOT_FOUND, "Parameter not found"));

                // Verify parameter belongs to the KPI
                if (!parameter.getKpi().getId().equals(kpiId)) {
                        throw ApiException.create(HttpStatus.BAD_REQUEST,
                                        "Parameter does not belong to the specified KPI");
                }

                // Check if progress already exists for this employee and parameter
                if (employeeKPIParameterRepository.existsByKpiParameterIdAndEmployeeId(parameterId, employeeId)) {
                        throw ApiException.create(HttpStatus.CONFLICT, "Progress already exists for this parameter");
                }

                // Create progress
                org.example.kpiservice.entity.EmployeeKPIParameter progress = org.example.kpiservice.entity.EmployeeKPIParameter
                                .builder()
                                .kpiParameter(parameter)
                                .employeeId(employeeId)
                                .progressValue(request.getProgressValue())
                                .notes(request.getNotes())
                                .comment(request.getComment())
                                .build();

                // Get current employee for audit fields
                Employee currentEmployee = employeeRepository.findByEmployeeId(employeeId)
                                .orElseThrow(() -> ApiException.create(HttpStatus.UNAUTHORIZED, "Employee not found"));

                // Set audit fields
                progress.setCreatedBy(currentEmployee.getId());
                progress.setUpdatedBy(currentEmployee.getId());

                // Save progress
                org.example.kpiservice.entity.EmployeeKPIParameter savedProgress = employeeKPIParameterRepository
                                .save(progress);

                log.info("Created progress for parameter {} of KPI {} by employee {}", parameterId, kpiId, employeeId);

                // Map to response
                return mapToProgressResponse(savedProgress);
        }

        @Override
        @Transactional(readOnly = true)
        public org.example.kpiservice.dtos.response.EmployeeKPIProgressResponse getCurrentUserProgress(Long kpiId,
                        Long parameterId, String employeeId) {
                // Verify KPI is assigned to this employee
                if (!employeeKPIRepository.existsByEmployeeIdAndKpiId(employeeId, kpiId)) {
                        throw ApiException.create(HttpStatus.NOT_FOUND, "KPI not found or not assigned to you");
                }

                // Get parameter to verify it belongs to the KPI
                org.example.kpiservice.entity.KPIParameter parameter = kpiParameterRepository.findById(parameterId)
                                .orElseThrow(() -> ApiException.create(HttpStatus.NOT_FOUND, "Parameter not found"));

                // Verify parameter belongs to the KPI
                if (!parameter.getKpi().getId().equals(kpiId)) {
                        throw ApiException.create(HttpStatus.BAD_REQUEST,
                                        "Parameter does not belong to the specified KPI");
                }

                // Get progress for current user and parameter
                org.example.kpiservice.entity.EmployeeKPIParameter progress = employeeKPIParameterRepository
                                .findByKpiParameterIdAndEmployeeId(parameterId, employeeId)
                                .orElseThrow(() -> ApiException.create(HttpStatus.NOT_FOUND,
                                                "Progress not found for this parameter"));

                // Map to response
                return mapToProgressResponse(progress);
        }

        @Override
        @Transactional
        public org.example.kpiservice.dtos.response.EmployeeKPIProgressResponse updateCurrentUserProgress(Long kpiId,
                        Long parameterId, org.example.kpiservice.dtos.request.CreateProgressRequest request,
                        String employeeId) {
                // Verify KPI is assigned to this employee
                if (!employeeKPIRepository.existsByEmployeeIdAndKpiId(employeeId, kpiId)) {
                        throw ApiException.create(HttpStatus.NOT_FOUND, "KPI not found or not assigned to you");
                }

                // Get parameter to verify it belongs to the KPI
                org.example.kpiservice.entity.KPIParameter parameter = kpiParameterRepository.findById(parameterId)
                                .orElseThrow(() -> ApiException.create(HttpStatus.NOT_FOUND, "Parameter not found"));

                // Verify parameter belongs to the KPI
                if (!parameter.getKpi().getId().equals(kpiId)) {
                        throw ApiException.create(HttpStatus.BAD_REQUEST,
                                        "Parameter does not belong to the specified KPI");
                }

                // Get existing progress
                org.example.kpiservice.entity.EmployeeKPIParameter progress = employeeKPIParameterRepository
                                .findByKpiParameterIdAndEmployeeId(parameterId, employeeId)
                                .orElseThrow(() -> ApiException.create(HttpStatus.NOT_FOUND,
                                                "Progress not found for this parameter"));

                // Verify progress belongs to current user (additional security check)
                if (!progress.getEmployeeId().equals(employeeId)) {
                        throw ApiException.create(HttpStatus.FORBIDDEN,
                                        "You are not authorized to update this progress");
                }

                // Update progress fields
                progress.setProgressValue(request.getProgressValue());
                progress.setNotes(request.getNotes());
                progress.setComment(request.getComment());

                // Get current employee for audit fields
                Employee currentEmployee = employeeRepository.findByEmployeeId(employeeId)
                                .orElseThrow(() -> ApiException.create(HttpStatus.UNAUTHORIZED, "Employee not found"));

                // Update audit field
                progress.setUpdatedBy(currentEmployee.getId());
                // updated_at will be auto-updated by JPA auditing

                // Save updated progress
                org.example.kpiservice.entity.EmployeeKPIParameter updatedProgress = employeeKPIParameterRepository
                                .save(progress);

                log.info("Updated progress for parameter {} of KPI {} by employee {}", parameterId, kpiId, employeeId);

                // Map to response
                return mapToProgressResponse(updatedProgress);
        }

        @Override
        @Transactional(readOnly = true)
        public List<org.example.kpiservice.dtos.response.EmployeeKPIProgressResponse> getCurrentUserKPIProgresses(
                        Long kpiId, String employeeId) {
                // Verify KPI is assigned to this employee
                if (!employeeKPIRepository.existsByEmployeeIdAndKpiId(employeeId, kpiId)) {
                        throw ApiException.create(HttpStatus.NOT_FOUND, "KPI not found or not assigned to you");
                }

                // Get all progress records for this KPI and employee
                List<org.example.kpiservice.entity.EmployeeKPIParameter> progresses = employeeKPIParameterRepository
                                .findAllByKpiIdAndEmployeeId(kpiId, employeeId);

                // Map to response
                return progresses.stream()
                                .map(this::mapToProgressResponse)
                                .toList();
        }

        @Override
        @Transactional(readOnly = true)
        public List<org.example.kpiservice.dtos.response.EmployeeKPIProgressResponse> getAssigneeProgresses(Long kpiId,
                        String assigneeId, String currentUserEmail) {
                // Get current employee
                Employee currentEmployee = employeeRepository.findByEmail(currentUserEmail)
                                .orElseThrow(() -> ApiException.create(HttpStatus.UNAUTHORIZED, "Employee not found"));

                // Get KPI
                KPI kpi = kpiRepository.findById(kpiId)
                                .orElseThrow(() -> ApiException.create(HttpStatus.NOT_FOUND, "KPI not found"));

                // Verify current user is the creator
                if (!kpi.getCreatedBy().equals(currentEmployee.getId())) {
                        throw ApiException.create(HttpStatus.FORBIDDEN,
                                        "Only the KPI creator can view assignee progress");
                }

                // Get all progress records for this KPI and assignee
                List<org.example.kpiservice.entity.EmployeeKPIParameter> progresses = employeeKPIParameterRepository
                                .findAllByKpiIdAndEmployeeId(kpiId, assigneeId);

                // Map to response
                return progresses.stream()
                                .map(this::mapToProgressResponse)
                                .toList();
        }

        @Override
        @Transactional(readOnly = true)
        public org.example.kpiservice.dtos.response.EmployeeKPIProgressResponse getAssigneeProgressById(Long kpiId,
                        String assigneeId, Long progressId, String currentUserEmail) {
                // Get current employee
                Employee currentEmployee = employeeRepository.findByEmail(currentUserEmail)
                                .orElseThrow(() -> ApiException.create(HttpStatus.UNAUTHORIZED, "Employee not found"));

                // Get KPI
                KPI kpi = kpiRepository.findById(kpiId)
                                .orElseThrow(() -> ApiException.create(HttpStatus.NOT_FOUND, "KPI not found"));

                // Verify current user is the creator
                if (!kpi.getCreatedBy().equals(currentEmployee.getId())) {
                        throw ApiException.create(HttpStatus.FORBIDDEN,
                                        "Only the KPI creator can view assignee progress");
                }

                // Get specific progress
                org.example.kpiservice.entity.EmployeeKPIParameter progress = employeeKPIParameterRepository
                                .findById(progressId)
                                .orElseThrow(() -> ApiException.create(HttpStatus.NOT_FOUND, "Progress not found"));

                // Verify progress belongs to the specified assignee
                if (!progress.getEmployeeId().equals(assigneeId)) {
                        throw ApiException.create(HttpStatus.BAD_REQUEST,
                                        "Progress does not belong to the specified employee");
                }

                // Verify progress belongs to the specified KPI
                if (!progress.getKpiParameter().getKpi().getId().equals(kpiId)) {
                        throw ApiException.create(HttpStatus.BAD_REQUEST,
                                        "Progress does not belong to the specified KPI");
                }

                // Map to response
                return mapToProgressResponse(progress);
        }

        @Override
        @Transactional
        public org.example.kpiservice.dtos.response.EmployeeKPIProgressResponse updateAssigneeProgressValue(Long kpiId,
                        String assigneeId, Long progressId, Integer progressValue, String currentUserEmail) {
                // Get current employee
                Employee currentEmployee = employeeRepository.findByEmail(currentUserEmail)
                                .orElseThrow(() -> ApiException.create(HttpStatus.UNAUTHORIZED, "Employee not found"));

                // Get KPI
                KPI kpi = kpiRepository.findById(kpiId)
                                .orElseThrow(() -> ApiException.create(HttpStatus.NOT_FOUND, "KPI not found"));

                // Verify current user is the creator
                if (!kpi.getCreatedBy().equals(currentEmployee.getId())) {
                        throw ApiException.create(HttpStatus.FORBIDDEN,
                                        "Only the KPI creator can update assignee progress");
                }

                // Get specific progress
                org.example.kpiservice.entity.EmployeeKPIParameter progress = employeeKPIParameterRepository
                                .findById(progressId)
                                .orElseThrow(() -> ApiException.create(HttpStatus.NOT_FOUND, "Progress not found"));

                // Verify progress belongs to the specified assignee
                if (!progress.getEmployeeId().equals(assigneeId)) {
                        throw ApiException.create(HttpStatus.BAD_REQUEST,
                                        "Progress does not belong to the specified employee");
                }

                // Verify progress belongs to the specified KPI
                if (!progress.getKpiParameter().getKpi().getId().equals(kpiId)) {
                        throw ApiException.create(HttpStatus.BAD_REQUEST,
                                        "Progress does not belong to the specified KPI");
                }

                // Update only progress value
                progress.setProgressValue(progressValue);

                // Update audit field
                progress.setUpdatedBy(currentEmployee.getId());
                // updated_at will be auto-updated by JPA auditing

                // Save updated progress
                org.example.kpiservice.entity.EmployeeKPIParameter updatedProgress = employeeKPIParameterRepository
                                .save(progress);

                log.info("KPI creator {} updated progress {} value to {} for assignee {}", currentUserEmail, progressId,
                                progressValue, assigneeId);

                // Map to response
                return mapToProgressResponse(updatedProgress);
        }

        private org.example.kpiservice.dtos.response.EmployeeKPIProgressResponse mapToProgressResponse(
                        org.example.kpiservice.entity.EmployeeKPIParameter progress) {
                return org.example.kpiservice.dtos.response.EmployeeKPIProgressResponse.builder()
                                .id(progress.getId())
                                .kpiParameterId(progress.getKpiParameter().getId())
                                .employeeId(progress.getEmployeeId())
                                .progressValue(progress.getProgressValue())
                                .notes(progress.getNotes())
                                .comment(progress.getComment())
                                .build();
        }

        private org.example.kpiservice.dtos.response.KPIParameterResponse mapToParameterResponse(
                        org.example.kpiservice.entity.KPIParameter parameter) {
                return org.example.kpiservice.dtos.response.KPIParameterResponse.builder()
                                .id(parameter.getId())
                                .name(parameter.getName())
                                .description(parameter.getDescription())
                                .targetValue(parameter.getTargetValue())
                                .build();
        }

        private KPIResponse mapToResponse(KPI kpi) {
                return KPIResponse.builder()
                                .id(kpi.getId())
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
