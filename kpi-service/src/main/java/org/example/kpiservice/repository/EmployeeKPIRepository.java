package org.example.kpiservice.repository;

import org.example.kpiservice.entity.EmployeeKPI;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeKPIRepository extends JpaRepository<EmployeeKPI, Long> {
    List<EmployeeKPI> findAllByEmployeeId(String employeeId);

    boolean existsByEmployeeIdAndKpiId(String employeeId, Long kpiId);

    Optional<EmployeeKPI> findByEmployeeIdAndKpiId(String employeeId, Long kpiId);

    List<EmployeeKPI> findAllByKpiId(Long kpiId);
}
