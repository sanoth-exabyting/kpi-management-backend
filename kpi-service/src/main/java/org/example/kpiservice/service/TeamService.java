package org.example.kpiservice.service;

import org.example.kpiservice.dtos.response.TeamResponse;

import java.util.List;


public interface TeamService {
    List<TeamResponse> getUserTeams(List<Long> teamIds);
}
