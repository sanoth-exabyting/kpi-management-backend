package org.example.kpiservice.service;

import org.example.kpiservice.dtos.request.CreateKPIRequest;
import org.example.kpiservice.dtos.request.UpdateKPIRequest;
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

    /**
     * Get user's ACTIVE KPIs
     *
     * @param teams User's teams from JWT
     * @return List of ACTIVE KPIs for user's teams
     */
    List<KPIResponse> getUserKPIs(List<Map<String, Object>> teams);

    /**
     * Get KPI by ID with team authorization check
     *
     * @param kpiId KPI ID
     * @param teams User's teams from JWT
     * @return KPI response
     */
    KPIResponse getKPIById(Long kpiId, List<Map<String, Object>> teams);

    /**
     * Update KPI (LEAD only)
     *
     * @param kpiId     KPI ID
     * @param request   Update request
     * @param userEmail Authenticated user's email
     * @param teams     User's teams from JWT
     * @return Updated KPI response
     */
    KPIResponse updateKPI(Long kpiId, UpdateKPIRequest request, String userEmail, List<Map<String, Object>> teams);
}
