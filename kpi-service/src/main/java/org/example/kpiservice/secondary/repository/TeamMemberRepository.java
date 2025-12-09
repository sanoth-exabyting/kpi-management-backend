package org.example.kpiservice.secondary.repository;

import org.example.kpiservice.secondary.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    Optional<TeamMember> findByEmployeeId(String employeeId);
}
