package org.example.kpiservice.service;

import org.example.kpiservice.dtos.request.CreateKPIParameterRequest;
import org.example.kpiservice.dtos.request.UpdateKPIParameterRequest;
import org.example.kpiservice.dtos.response.KPIParameterResponse;

import java.util.List;
import java.util.Map;

/**
 * Service for KPI parameter operations
 */
public interface KPIParameterService {

    /**
     * Create KPI parameter (LEAD only)
     *
     * @param kpiId     KPI ID
     * @param request   Create request
     * @param userEmail Authenticated user's email
     * @param teams     User's teams from JWT
     * @return Created parameter response
     */
    KPIParameterResponse createKPIParameter(Long kpiId, CreateKPIParameterRequest request, String userEmail,
            List<Map<String, Object>> teams);

    /**
     * Get KPI parameters (team member access)
     *
     * @param kpiId KPI ID
     * @param teams User's teams from JWT
     * @return List of parameters
     */
    List<KPIParameterResponse> getKPIParameters(Long kpiId, List<Map<String, Object>> teams);

    /**
     * Get KPI parameter by ID (team member access)
     *
     * @param kpiId       KPI ID
     * @param parameterId Parameter ID
     * @param teams       User's teams from JWT
     * @return Parameter response
     */
    KPIParameterResponse getKPIParameterById(Long kpiId, Long parameterId, List<Map<String, Object>> teams);

    /**
     * Update KPI parameter (LEAD only)
     *
     * @param kpiId       KPI ID
     * @param parameterId Parameter ID
     * @param request     Update request
     * @param userEmail   Authenticated user's email
     * @param teams       User's teams from JWT
     * @return Updated parameter response
     */
    KPIParameterResponse updateKPIParameter(Long kpiId, Long parameterId, UpdateKPIParameterRequest request,
            String userEmail, List<Map<String, Object>> teams);

    /**
     * Delete KPI parameter (LEAD only)
     *
     * @param kpiId       KPI ID
     * @param parameterId Parameter ID
     * @param userEmail   Authenticated user's email
     * @param teams       User's teams from JWT
     */
    void deleteKPIParameter(Long kpiId, Long parameterId, String userEmail, List<Map<String, Object>> teams);
}
