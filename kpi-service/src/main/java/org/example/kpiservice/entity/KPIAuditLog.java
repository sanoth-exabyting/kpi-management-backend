package org.example.kpiservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "kpi_audit_logs", indexes = {
        @Index(name = "idx_kpi_audit_log_kpi_id", columnList = "kpi_id"),
        @Index(name = "idx_kpi_audit_log_employee_id", columnList = "employee_id"),
        @Index(name = "idx_kpi_audit_log_kpi_parameter_id", columnList = "kpi_parameter_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class KPIAuditLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kpi_id", nullable = false)
    private KPI kpi;

    @Column(name = "employee_id", nullable = false)
    @JdbcTypeCode(SqlTypes.BIGINT)
    private Long employeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kpi_parameter_id")
    private KPIParameter kpiParameter;

    @Column(name = "old_value", columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private String newValue;
}
