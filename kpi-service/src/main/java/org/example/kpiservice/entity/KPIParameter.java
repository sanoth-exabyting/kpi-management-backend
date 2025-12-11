package org.example.kpiservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "kpi_parameters", indexes = {
        @Index(name = "idx_kpi_parameter_kpi_id", columnList = "kpi_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class KPIParameter extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kpi_id", nullable = false)
    private KPI kpi;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "target_value", nullable = false)
    @Min(0)
    @Max(100)
    @lombok.Builder.Default
    private Integer targetValue = 0;

    @Column(name = "is_required", nullable = false)
    @lombok.Builder.Default
    private Boolean isRequired = true;
}
