package org.example.kpiservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "employee_kpis", indexes = {
        @Index(name = "idx_employee_kpi_employee_id", columnList = "employee_id"),
        @Index(name = "idx_employee_kpi_kpi_id", columnList = "kpi_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class EmployeeKPI extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kpi_id", nullable = false)
    private KPI kpi;

    @Column(name = "employee_id", nullable = false, length = 50)
    private String employeeId;
}
