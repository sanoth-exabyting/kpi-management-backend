package org.example.kpiservice.service;

import org.example.kpiservice.dtos.request.CreateKPIParameterRequest;
import org.example.kpiservice.dtos.request.CreateProgressRequest;
import org.example.kpiservice.dtos.request.UpdateKPIParameterRequest;
import org.example.kpiservice.dtos.response.EmployeeKPIProgressResponse;
import org.example.kpiservice.dtos.response.KPIParameterResponse;

import java.util.List;
import java.util.Map;


public interface KPIParameterService {

    KPIParameterResponse createKPIParameter(Long kpiId, CreateKPIParameterRequest request, String userEmail);

    List<KPIParameterResponse> getKPIParameters(Long kpiId, String userEmail);

    KPIParameterResponse getKPIParameterById(Long kpiId, Long parameterId, String userEmail);

    KPIParameterResponse updateKPIParameter(Long kpiId, Long parameterId, UpdateKPIParameterRequest request,
            String userEmail);

    void deleteKPIParameter(Long kpiId, Long parameterId, String userEmail);

    EmployeeKPIProgressResponse createProgress(Long kpiId, Long parameterId, CreateProgressRequest request,
            String userEmail, List<Map<String, Object>> teams);
}
