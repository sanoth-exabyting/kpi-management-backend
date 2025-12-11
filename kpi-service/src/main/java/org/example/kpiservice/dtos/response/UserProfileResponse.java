package org.example.kpiservice.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private String employeeId;
    private String name;
    private String email;
    private String designation;
    private org.example.kpiservice.secondary.enums.EmployeeStatus status;
    private List<UserTeamInfo> teams;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserTeamInfo {
        private Long teamId;
        private String teamName;
        private String role;
    }
}
