package org.example.kpiservice.repository;

import org.example.kpiservice.entity.KPI;
import org.example.kpiservice.enums.KPIStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KPIRepository extends JpaRepository<KPI, Long> {

    List<KPI> findAllByCreatedBy(Long createdBy);

    List<KPI> findAllByStatusAndIsDeletedFalse(KPIStatus status);
}
