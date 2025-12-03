package org.example.kpiservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "employee_kpi_parameters", indexes = {
        @Index(name = "idx_emp_kpi_param_kpi_parameter_id", columnList = "kpi_parameter_id"),
        @Index(name = "idx_emp_kpi_param_employee_id", columnList = "employee_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class EmployeeKPIParameter extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kpi_parameter_id", nullable = false)
    private KPIParameter kpiParameter;

    @Column(name = "employee_id", nullable = false)
    @JdbcTypeCode(SqlTypes.BIGINT)
    private Long employeeId;

    @Min(0)
    @Max(100)
    @Column(name = "progress_value")
    private Integer progressValue;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;
}
