package org.example.kpiservice.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kpiservice.service.KPIReportService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class KPIReportScheduler {

    private final KPIReportService kpiReportService;

    @Scheduled(cron = "${app.scheduling.kpi-report.cron}")
    public void scheduleMonthlyKPIReport() {
        log.info("Starting scheduled monthly KPI report generation...");
        try {
            kpiReportService.generateMonthlyReports();
            log.info("Completed scheduled monthly KPI report generation.");
        } catch (Exception e) {
            log.error("Failed to generate monthly KPI reports", e);
        }
    }
}
