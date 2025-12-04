package org.example.kpiservice.secondary.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.kpiservice.secondary.enums.ActiveStatus;
import org.example.kpiservice.secondary.enums.MemberRole;

import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "team_member")
public class TeamMember {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "team_id")
    private Long teamId;

    @Column(name = "employee_id")
    private String employeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private MemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ActiveStatus status;

    @Column(name = "created_on")
    private ZonedDateTime createdOn;

    @Column(name = "last_updated_on")
    private ZonedDateTime lastUpdatedOn;
}
