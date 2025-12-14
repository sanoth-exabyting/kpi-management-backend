package org.example.kpiservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kpiservice.dtos.request.CreateKPIParameterRequest;
import org.example.kpiservice.dtos.request.CreateProgressRequest;
import org.example.kpiservice.dtos.request.UpdateKPIParameterRequest;
import org.example.kpiservice.dtos.response.EmployeeKPIProgressResponse;
import org.example.kpiservice.dtos.response.KPIParameterResponse;
import org.example.kpiservice.entity.Employee;
import org.example.kpiservice.entity.EmployeeKPIParameter;
import org.example.kpiservice.entity.KPI;
import org.example.kpiservice.entity.KPIParameter;
import org.example.kpiservice.exception.ApiException;
import org.example.kpiservice.repository.EmployeeKPIParameterRepository;
import org.example.kpiservice.repository.EmployeeRepository;
import org.example.kpiservice.repository.KPIParameterRepository;
import org.example.kpiservice.repository.KPIRepository;
import org.example.kpiservice.service.KPIParameterService;
import org.example.kpiservice.util.TeamAccessValidator;
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
        private final EmployeeKPIParameterRepository employeeKPIParameterRepository;
        private final KPIRepository kpiRepository;
        private final EmployeeRepository employeeRepository;
        private final TeamAccessValidator teamAccessValidator;

        @Override
        @Transactional
        public KPIParameterResponse createKPIParameter(Long kpiId, CreateKPIParameterRequest request, String userEmail) {
                // Get employee by email
                Employee employee = employeeRepository.findByEmail(userEmail)
                                .orElseThrow(() -> ApiException.create(HttpStatus.UNAUTHORIZED, "Employee not found"));

                // Find KPI by ID
                KPI kpi = kpiRepository.findById(kpiId)
                                .orElseThrow(() -> ApiException.create(HttpStatus.NOT_FOUND, "KPI not found"));

                // Validate access (Creator only)
                if (!kpi.getCreatedBy().equals(employee.getId())) {
                        throw ApiException.create(HttpStatus.FORBIDDEN,
                                        "Only the creator of the KPI can add parameters");
                }

                // Create KPI parameter
                KPIParameter parameter = KPIParameter.builder()
                                .kpi(kpi)
                                .name(request.getName())
                                .description(request.getDescription())
                                .targetValue(request.getTargetValue())
                                .isRequired(request.getIsRequired())
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

        @Override
        public List<KPIParameterResponse> getKPIParameters(Long kpiId, String userEmail) {
                // Find KPI by ID
                KPI kpi = kpiRepository.findById(kpiId)
                                .orElseThrow(() -> ApiException.create(HttpStatus.NOT_FOUND, "KPI not found"));

                // Access: Open for authenticated users

                // Get all parameters for the KPI
                List<KPIParameter> parameters = kpiParameterRepository.findAllByKpiId(kpiId);

                log.info("Retrieved {} parameters for KPI: {}", parameters.size(), kpiId);

                return parameters.stream()
                                .map(this::mapToResponse)
                                .toList();
        }

        @Override
        public KPIParameterResponse getKPIParameterById(Long kpiId, Long parameterId, String userEmail) {
                // Find KPI by ID
                KPI kpi = kpiRepository.findById(kpiId)
                                .orElseThrow(() -> ApiException.create(HttpStatus.NOT_FOUND, "KPI not found"));

                // Access: Open for authenticated users

                // Find parameter by ID
                KPIParameter parameter = kpiParameterRepository.findById(parameterId)
                                .orElseThrow(() -> ApiException.create(HttpStatus.NOT_FOUND, "Parameter not found"));

                // Verify parameter belongs to the specified KPI
                if (!parameter.getKpi().getId().equals(kpiId)) {
                        throw ApiException.create(HttpStatus.BAD_REQUEST,
                                        "Parameter does not belong to the specified KPI");
                }

                log.info("Retrieved parameter: {} for KPI: {}", parameterId, kpiId);

                return mapToResponse(parameter);
        }

        @Override
        @Transactional
        public KPIParameterResponse updateKPIParameter(Long kpiId, Long parameterId, UpdateKPIParameterRequest request,
                        String userEmail) {
                // Get employee by email
                Employee employee = employeeRepository.findByEmail(userEmail)
                                .orElseThrow(() -> ApiException.create(HttpStatus.UNAUTHORIZED, "Employee not found"));

                // Find KPI by ID
                KPI kpi = kpiRepository.findById(kpiId)
                                .orElseThrow(() -> ApiException.create(HttpStatus.NOT_FOUND, "KPI not found"));

                // Validate access (Creator only)
                if (!kpi.getCreatedBy().equals(employee.getId())) {
                        throw ApiException.create(HttpStatus.FORBIDDEN,
                                        "Only the creator of the KPI can update parameters");
                }

                // Find parameter by ID
                KPIParameter parameter = kpiParameterRepository.findById(parameterId)
                                .orElseThrow(() -> ApiException.create(HttpStatus.NOT_FOUND, "Parameter not found"));

                // Verify parameter belongs to the specified KPI
                if (!parameter.getKpi().getId().equals(kpiId)) {
                        throw ApiException.create(HttpStatus.BAD_REQUEST,
                                        "Parameter does not belong to the specified KPI");
                }

                // Update only provided fields (partial update)
                if (request.getName() != null) {
                        parameter.setName(request.getName());
                }
                if (request.getDescription() != null) {
                        parameter.setDescription(request.getDescription());
                }
                if (request.getTargetValue() != null) {
                        parameter.setTargetValue(request.getTargetValue());
                }
                if (request.getIsRequired() != null) {
                        parameter.setIsRequired(request.getIsRequired());
                }

                // Update audit fields (created_by and created_at remain unchanged)
                parameter.setUpdatedBy(employee.getId());
                // updated_at will be automatically set by @PreUpdate in BaseEntity

                // Save updated parameter
                KPIParameter updatedParameter = kpiParameterRepository.save(parameter);

                log.info("KPI parameter updated: {} for KPI: {} by employee: {}", parameterId, kpiId,
                                employee.getEmployeeId());

                return mapToResponse(updatedParameter);
        }

        @Override
        @Transactional
        public void deleteKPIParameter(Long kpiId, Long parameterId, String userEmail) {
                // Get employee by email
                Employee employee = employeeRepository.findByEmail(userEmail)
                                .orElseThrow(() -> ApiException.create(HttpStatus.UNAUTHORIZED, "Employee not found"));

                // Find KPI by ID
                KPI kpi = kpiRepository.findById(kpiId)
                                .orElseThrow(() -> ApiException.create(HttpStatus.NOT_FOUND, "KPI not found"));

                // Validate access (Creator only)
                if (!kpi.getCreatedBy().equals(employee.getId())) {
                        throw ApiException.create(HttpStatus.FORBIDDEN,
                                        "Only the creator of the KPI can delete parameters");
                }

                // Find parameter by ID
                KPIParameter parameter = kpiParameterRepository.findById(parameterId)
                                .orElseThrow(() -> ApiException.create(HttpStatus.NOT_FOUND, "Parameter not found"));

                // Verify parameter belongs to the specified KPI
                if (!parameter.getKpi().getId().equals(kpiId)) {
                        throw ApiException.create(HttpStatus.BAD_REQUEST,
                                        "Parameter does not belong to the specified KPI");
                }

                // Delete parameter
                kpiParameterRepository.delete(parameter);

                log.info("KPI parameter deleted: {} from KPI: {} by employee: {}", parameterId, kpiId,
                                employee.getEmployeeId());
        }

        @Override
        @Transactional
        public EmployeeKPIProgressResponse createProgress(Long kpiId, Long parameterId, CreateProgressRequest request,
                        String userEmail, List<Map<String, Object>> teams) {
                // Get employee by email
                Employee employee = employeeRepository.findByEmail(userEmail)
                                .orElseThrow(() -> ApiException.create(HttpStatus.UNAUTHORIZED, "Employee not found"));

                // Find KPI by ID
                KPI kpi = kpiRepository.findById(kpiId)
                                .orElseThrow(() -> ApiException.create(HttpStatus.NOT_FOUND, "KPI not found"));

                // Access: Open for authenticated users (Self-assignment/progress)

                // Find parameter by ID
                KPIParameter parameter = kpiParameterRepository.findById(parameterId)
                                .orElseThrow(() -> ApiException.create(HttpStatus.NOT_FOUND, "Parameter not found"));

                // Verify parameter belongs to the specified KPI
                if (!parameter.getKpi().getId().equals(kpiId)) {
                        throw ApiException.create(HttpStatus.BAD_REQUEST,
                                        "Parameter does not belong to the specified KPI");
                }

                // Check if employee already has progress for this parameter
                boolean progressExists = employeeKPIParameterRepository.existsByKpiParameterIdAndEmployeeId(
                                parameterId, employee.getEmployeeId());

                if (progressExists) {
                        throw ApiException.create(HttpStatus.CONFLICT,
                                        "Progress already exists for this parameter. Each employee can only create progress once per parameter.");
                }

                // Create employee KPI parameter progress
                EmployeeKPIParameter progress = EmployeeKPIParameter.builder()
                                .kpiParameter(parameter)
                                .employeeId(employee.getEmployeeId())
                                .progressValue(request.getProgressValue())
                                .notes(request.getNotes())
                                .comment(request.getComment())
                                .build();

                // Set audit fields
                progress.setCreatedBy(employee.getId());
                progress.setUpdatedBy(employee.getId());

                // Save progress
                EmployeeKPIParameter savedProgress = employeeKPIParameterRepository.save(progress);

                log.info("Employee progress created: {} for parameter: {} by employee: {}", savedProgress.getId(),
                                parameterId, employee.getEmployeeId());

                return mapProgressToResponse(savedProgress);
        }

        private KPIParameterResponse mapToResponse(KPIParameter parameter) {
                return KPIParameterResponse.builder()
                                .id(parameter.getId())
                                .kpiId(parameter.getKpi().getId())
                                .name(parameter.getName())
                                .description(parameter.getDescription())
                                .targetValue(parameter.getTargetValue())
                                .isRequired(parameter.getIsRequired())
                                .createdBy(parameter.getCreatedBy())
                                .createdAt(parameter.getCreatedAt())
                                .updatedBy(parameter.getUpdatedBy())
                                .updatedAt(parameter.getUpdatedAt())
                                .build();
        }

        private EmployeeKPIProgressResponse mapProgressToResponse(EmployeeKPIParameter progress) {
                return EmployeeKPIProgressResponse.builder()
                                .id(progress.getId())
                                .kpiParameterId(progress.getKpiParameter().getId())
                                .employeeId(progress.getEmployeeId())
                                .progressValue(progress.getProgressValue())
                                .notes(progress.getNotes())
                                .comment(progress.getComment())
                                .createdBy(progress.getCreatedBy())
                                .createdAt(progress.getCreatedAt())
                                .updatedBy(progress.getUpdatedBy())
                                .updatedAt(progress.getUpdatedAt())
                                .build();
        }

}