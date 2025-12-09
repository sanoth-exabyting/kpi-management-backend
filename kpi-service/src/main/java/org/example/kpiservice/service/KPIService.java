package org.example.kpiservice.service;

import org.example.kpiservice.dtos.request.CreateKPIRequest;
import org.example.kpiservice.dtos.response.KPIResponse;

import java.util.List;
import java.util.Map;

/**
 * Service for handling KPI operations
 */
public interface KPIService {

    /**
     * Create a new KPI
     *
     * @param request   Create KPI request
     * @param userEmail Authenticated user's email
     * @param teams     User's teams with roles from JWT
     * @return Created KPI response
     */
    KPIResponse createKPI(CreateKPIRequest request, String userEmail, List<Map<String, Object>> teams);
}
