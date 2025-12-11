package org.example.kpiservice.dtos.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssignEmployeesToKPIRequest {

    @NotNull(message = "Employee IDs cannot be null")
    @NotEmpty(message = "Employee IDs cannot be empty")
    private List<String> employeeIds;
}
