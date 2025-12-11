package org.example.kpiservice.service;

import org.example.kpiservice.dtos.request.CreateKPIRequest;
import org.example.kpiservice.dtos.request.UpdateKPIRequest;
import org.example.kpiservice.dtos.response.KPIResponse;
import org.example.kpiservice.dtos.request.AssignEmployeesToKPIRequest;

import java.util.List;

public interface KPIService {

    KPIResponse createKPI(CreateKPIRequest request, String userEmail);

    List<KPIResponse> getUserKPIs(String userEmail);

    KPIResponse getKPIById(Long kpiId, String userEmail);

    KPIResponse updateKPI(Long kpiId, UpdateKPIRequest request, String userEmail);

    void deleteKPI(Long kpiId, String userEmail);

    void assignEmployeesToKPI(Long kpiId, AssignEmployeesToKPIRequest request, String userEmail,
            jakarta.servlet.http.HttpServletRequest httpRequest);

    List<org.example.kpiservice.dtos.response.EmployeeResponse> getKPIAssignees(Long kpiId, String userEmail);

    org.example.kpiservice.dtos.response.EmployeeResponse getKPIAssigneeById(Long kpiId, String employeeId,
            String userEmail);

    void removeKPIAssignee(Long kpiId, String employeeId, String userEmail);

    List<KPIResponse> getCurrentUserKPIs(String employeeId);

    KPIResponse getCurrentUserKPIById(Long kpiId, String employeeId);

    List<org.example.kpiservice.dtos.response.KPIParameterResponse> getCurrentUserKPIParameters(Long kpiId,
            String employeeId);

    org.example.kpiservice.dtos.response.KPIParameterResponse getCurrentUserKPIParameterById(Long kpiId,
            Long parameterId, String employeeId);

    org.example.kpiservice.dtos.response.EmployeeKPIProgressResponse createCurrentUserProgress(Long kpiId,
            Long parameterId, org.example.kpiservice.dtos.request.CreateProgressRequest request, String employeeId);

    org.example.kpiservice.dtos.response.EmployeeKPIProgressResponse getCurrentUserProgress(Long kpiId,
            Long parameterId, String employeeId);

    org.example.kpiservice.dtos.response.EmployeeKPIProgressResponse updateCurrentUserProgress(Long kpiId,
            Long parameterId, org.example.kpiservice.dtos.request.CreateProgressRequest request, String employeeId);

    List<org.example.kpiservice.dtos.response.EmployeeKPIProgressResponse> getCurrentUserKPIProgresses(Long kpiId,
            String employeeId);

    List<org.example.kpiservice.dtos.response.EmployeeKPIProgressResponse> getAssigneeProgresses(Long kpiId,
            String assigneeId, String currentUserEmail);

    org.example.kpiservice.dtos.response.EmployeeKPIProgressResponse getAssigneeProgressById(Long kpiId,
            String assigneeId, Long progressId, String currentUserEmail);

    org.example.kpiservice.dtos.response.EmployeeKPIProgressResponse updateAssigneeProgressValue(Long kpiId,
            String assigneeId, Long progressId, Integer progressValue, String currentUserEmail);
}
