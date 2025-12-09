package org.example.kpiservice.repository;

import org.example.kpiservice.entity.KPI;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KPIRepository extends JpaRepository<KPI, Long> {
}
