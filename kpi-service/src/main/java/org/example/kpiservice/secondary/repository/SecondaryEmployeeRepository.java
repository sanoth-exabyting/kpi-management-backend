package org.example.kpiservice.secondary.repository;

import org.example.kpiservice.secondary.entity.Employee;
import org.example.kpiservice.secondary.enums.EmployeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SecondaryEmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmail(String email);

    Optional<Employee> findByEmployeeId(String employeeId);

    Optional<Employee> findByEmailAndStatusNot(String email, EmployeeStatus status);

    List<Employee> findAllByEmployeeIdIn(List<String> employeeIds);

    List<Employee> findAllByEmployeeIdInAndStatusNot(List<String> employeeIds, EmployeeStatus status);
}
