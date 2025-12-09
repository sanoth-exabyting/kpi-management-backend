package org.example.kpiservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kpiservice.dtos.response.TeamResponse;
import org.example.kpiservice.secondary.entity.Team;
import org.example.kpiservice.secondary.repository.TeamRepository;
import org.example.kpiservice.service.TeamService;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;

    @Override
    public List<TeamResponse> getUserTeams(List<Long> teamIds) {
        if (teamIds == null || teamIds.isEmpty()) {
            log.warn("Empty team IDs list provided");
            return List.of();
        }

        List<Team> teams = teamRepository.findAllByIdIn(teamIds);

        log.info("Retrieved {} teams for user", teams.size());

        return teams.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private TeamResponse mapToResponse(Team team) {
        return TeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .status(team.getStatus() != null ? team.getStatus().name() : null)
                .createdOn(team.getCreatedOn())
                .build();
    }
}
