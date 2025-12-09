package org.example.kpiservice.secondary.repository;

import org.example.kpiservice.secondary.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    List<Team> findAllByIdIn(List<Long> teamIds);
}
