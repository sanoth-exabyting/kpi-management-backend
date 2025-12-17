package org.example.kpiservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kpiservice.entity.Employee;
import org.example.kpiservice.entity.EmployeeKPIParameter;
import org.example.kpiservice.entity.KPI;
import org.example.kpiservice.entity.KPIParameter;
import org.example.kpiservice.enums.KPIStatus;
import org.example.kpiservice.repository.EmployeeKPIParameterRepository;
import org.example.kpiservice.repository.EmployeeRepository;
import org.example.kpiservice.repository.KPIParameterRepository;
import org.example.kpiservice.repository.KPIRepository;
import org.example.kpiservice.secondary.repository.SecondaryEmployeeRepository;
import org.example.kpiservice.service.EmailService;
import org.example.kpiservice.service.KPIReportService;
import org.example.kpiservice.service.PdfGenerationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KPIReportServiceImpl implements KPIReportService {

    private final KPIRepository kpiRepository;
    private final EmployeeRepository employeeRepository;
    private final KPIParameterRepository kpiParameterRepository;
    private final EmployeeKPIParameterRepository employeeKPIParameterRepository;
    private final SecondaryEmployeeRepository secondaryEmployeeRepository;
    private final PdfGenerationService pdfGenerationService;
    private final EmailService emailService;

    @Override
    @Transactional(readOnly = true)
    public void generateMonthlyReports() {
        log.info("Starting monthly KPI report generation");

        // 1. Fetch Active KPIs
        List<KPI> activeKpis = kpiRepository.findAllByStatusAndIsDeletedFalse(KPIStatus.ACTIVE);
        if (activeKpis.isEmpty()) {
            log.info("No active KPIs found. Skipping report generation.");
            return;
        }

        // Sort by most recently created first
        activeKpis.sort(Comparator.comparing(KPI::getCreatedAt).reversed());

        // 2. Group by Creator
        Map<Long, List<KPI>> kpisByCreator = activeKpis.stream()
                .collect(Collectors.groupingBy(KPI::getCreatedBy));

        // 3. Fetch all Creators
        List<Employee> creators = employeeRepository.findAllById(kpisByCreator.keySet());
        Map<Long, Employee> creatorMap = creators.stream()
                .collect(Collectors.toMap(Employee::getId, e -> e));

        // 4. Fetch all Parameters for these KPIs (Batch fetch)
        List<Long> allKpiIds = activeKpis.stream().map(KPI::getId).toList();
        List<KPIParameter> allParameters = kpiParameterRepository.findAllByKpiIdIn(allKpiIds);
        Map<Long, List<KPIParameter>> kpiParametersMap = allParameters.stream()
                .collect(Collectors.groupingBy(p -> p.getKpi().getId()));

        // 5. Fetch all Progress for these KPIs (Batch fetch)
        List<EmployeeKPIParameter> allProgress = employeeKPIParameterRepository.findAllByKpiParameterKpiIdIn(allKpiIds);
        Map<Long, List<EmployeeKPIParameter>> parameterProgressMap = allProgress.stream()
                .collect(Collectors.groupingBy(p -> p.getKpiParameter().getId()));

        // 6. Fetch Assignee Names
        List<String> assigneeIds = allProgress.stream()
                .map(EmployeeKPIParameter::getEmployeeId)
                .distinct()
                .toList();

        Map<String, String> assigneeNameMap = Map.of();
        if (!assigneeIds.isEmpty()) {
            List<org.example.kpiservice.secondary.entity.Employee> assignees = secondaryEmployeeRepository
                    .findAllByEmployeeIdIn(assigneeIds);
            assigneeNameMap = assignees.stream()
                    .collect(Collectors.toMap(
                            org.example.kpiservice.secondary.entity.Employee::getEmployeeId,
                            org.example.kpiservice.secondary.entity.Employee::getName,
                            (existing, replacement) -> existing // Keep existing in case of duplicates
                    ));
        }

        // 7. Generate and Send Reports
        for (Map.Entry<Long, List<KPI>> entry : kpisByCreator.entrySet()) {
            Long creatorId = entry.getKey();
            List<KPI> creatorKpis = entry.getValue();
            Employee creator = creatorMap.get(creatorId);

            if (creator == null) {
                log.warn("Creator with ID {} not found. Skipping report.", creatorId);
                continue;
            }

            try {
                generateAndSendReport(creator, creatorKpis, kpiParametersMap, parameterProgressMap, assigneeNameMap);
            } catch (Exception e) {
                log.error("Failed to generate/send report for creator {}", creator.getEmail(), e);
            }
        }

        log.info("Monthly KPI report generation completed");
    }

    private void generateAndSendReport(Employee creator, List<KPI> kpis,
            Map<Long, List<KPIParameter>> kpiParametersMap,
            Map<Long, List<EmployeeKPIParameter>> parameterProgressMap,
            Map<String, String> assigneeNameMap) {

        Context context = new Context();
        context.setVariable("creatorName", creator.getName());
        context.setVariable("reportDate", LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        context.setVariable("kpis", kpis);
        context.setVariable("kpiParametersMap", kpiParametersMap);
        context.setVariable("parameterProgressMap", parameterProgressMap);
        context.setVariable("assigneeNameMap", assigneeNameMap);

        byte[] pdfBytes = pdfGenerationService.generatePdfFromTemplate("kpi_monthly_report", context);

        String subject = "Monthly KPI Report - " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        String body = "Dear " + creator.getName() + ",<br><br>" +
                "Please find attached your monthly KPI report.<br><br>" +
                "Best regards,<br>KPI Management System";
        String filename = "KPI_Report_" + LocalDate.now().format(DateTimeFormatter.ofPattern("MMM_yyyy")) + ".pdf";

        emailService.sendEmailWithAttachment(creator.getEmail(), subject, body, filename, pdfBytes);
    }
}
