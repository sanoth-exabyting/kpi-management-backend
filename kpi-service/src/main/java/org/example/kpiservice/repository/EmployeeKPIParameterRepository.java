package org.example.kpiservice.repository;

import org.example.kpiservice.entity.EmployeeKPIParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeKPIParameterRepository extends JpaRepository<EmployeeKPIParameter, Long> {

    boolean existsByKpiParameterIdAndEmployeeId(Long kpiParameterId, String employeeId);

    @Query("SELECT ekp FROM EmployeeKPIParameter ekp WHERE ekp.kpiParameter.kpi.id = :kpiId AND ekp.employeeId = :employeeId")
    List<EmployeeKPIParameter> findAllByKpiIdAndEmployeeId(@Param("kpiId") Long kpiId,
            @Param("employeeId") String employeeId);

    java.util.Optional<EmployeeKPIParameter> findByKpiParameterIdAndEmployeeId(Long kpiParameterId, String employeeId);

    List<EmployeeKPIParameter> findAllByKpiParameterKpiIdIn(List<Long> kpiIds);
}
