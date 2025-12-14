package org.example.kpiservice.service;

import org.example.kpiservice.dtos.request.CreateKPIRequest;
import org.example.kpiservice.dtos.request.UpdateKPIRequest;
import org.example.kpiservice.dtos.response.EmployeeKPIProgressResponse;
import org.example.kpiservice.dtos.response.EmployeeResponse;
import org.example.kpiservice.dtos.response.KPIParameterResponse;
import org.example.kpiservice.dtos.response.KPIResponse;
import org.example.kpiservice.dtos.request.AssignEmployeesToKPIRequest;
import org.example.kpiservice.dtos.request.CreateProgressRequest;

import java.util.List;

public interface KPIService {

    KPIResponse createKPI(CreateKPIRequest request, String userEmail);

    List<KPIResponse> getUserKPIs(String userEmail);

    KPIResponse getKPIById(Long kpiId, String userEmail);

    KPIResponse updateKPI(Long kpiId, UpdateKPIRequest request, String userEmail);

    void deleteKPI(Long kpiId, String userEmail);

    void assignEmployeesToKPI(Long kpiId, AssignEmployeesToKPIRequest request, String userEmail,
            jakarta.servlet.http.HttpServletRequest httpRequest);

    List<EmployeeResponse> getKPIAssignees(Long kpiId, String userEmail);

    EmployeeResponse getKPIAssigneeById(Long kpiId, String employeeId,
            String userEmail);

    void removeKPIAssignee(Long kpiId, String employeeId, String userEmail);

    List<KPIResponse> getCurrentUserKPIs(String employeeId);

    KPIResponse getCurrentUserKPIById(Long kpiId, String employeeId);

    List<KPIParameterResponse> getCurrentUserKPIParameters(Long kpiId,
                                                           String employeeId);

    KPIParameterResponse getCurrentUserKPIParameterById(Long kpiId,
            Long parameterId, String employeeId);

    EmployeeKPIProgressResponse createCurrentUserProgress(Long kpiId,
                                                          Long parameterId, CreateProgressRequest request, String employeeId);

    EmployeeKPIProgressResponse getCurrentUserProgress(Long kpiId,
            Long parameterId, String employeeId);

    EmployeeKPIProgressResponse updateCurrentUserProgress(Long kpiId,
            Long parameterId, CreateProgressRequest request, String employeeId);

    List<EmployeeKPIProgressResponse> getCurrentUserKPIProgresses(Long kpiId,
            String employeeId);

    List<EmployeeKPIProgressResponse> getAssigneeProgresses(Long kpiId,
            String assigneeId, String currentUserEmail);

    EmployeeKPIProgressResponse getAssigneeProgressById(Long kpiId,
            String assigneeId, Long progressId, String currentUserEmail);

    EmployeeKPIProgressResponse updateAssigneeProgressValue(Long kpiId,
            String assigneeId, Long progressId, Integer progressValue, String currentUserEmail);
}
