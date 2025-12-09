package org.example.kpiservice.service;

import org.example.kpiservice.dtos.response.TeamResponse;

import java.util.List;

/**
 * Service for handling team operations
 */
public interface TeamService {

    /**
     * Get teams by team IDs
     *
     * @param teamIds List of team IDs
     * @return List of TeamResponse containing team details
     */
    List<TeamResponse> getUserTeams(List<Long> teamIds);
}
