package org.example.kpiservice.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.kpiservice.secondary.enums.EmployeeStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {
    private String employeeId;
    private String name;
    private String email;
    private String designation;
    private EmployeeStatus status;
}
