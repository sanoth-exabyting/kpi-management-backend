package org.example.kpiservice.secondary.repository;

import org.example.kpiservice.secondary.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    List<TeamMember> findAllByEmployeeId(String employeeId);

    List<TeamMember> findAllByTeamIdIn(List<Long> teamIds);
}
