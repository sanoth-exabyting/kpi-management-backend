package org.example.kpiservice.service;

import org.example.kpiservice.dtos.request.CreateKPIParameterRequest;
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
}
