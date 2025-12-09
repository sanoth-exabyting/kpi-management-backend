package org.example.kpiservice.repository;

import org.example.kpiservice.entity.KPIParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KPIParameterRepository extends JpaRepository<KPIParameter, Long> {

    List<KPIParameter> findAllByKpiId(Long kpiId);
}
