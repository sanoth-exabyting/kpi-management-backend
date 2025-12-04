package org.example.kpiservice.secondary.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.kpiservice.secondary.enums.BloodGroup;
import org.example.kpiservice.secondary.enums.EmployeeStatus;
import org.example.kpiservice.secondary.enums.Gender;

import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "employee")
public class Employee {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "employee_id")
    private String employeeId;

    @Column(name = "email")
    private String email;

    @Column(name = "partner_id")
    private Long partnerId;

    @Column(name = "supervisor_id")
    private String supervisorId;

    @Column(name = "date_of_birth")
    private ZonedDateTime dateOfBirth;

    @Column(name = "joining_date")
    private ZonedDateTime joiningDate;

    @Column(name = "designation")
    private String designation;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "name")
    private String name;

    @Column(name = "nid")
    private String nid;

    @Column(name = "tin_number")
    private String tinNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_group")
    private BloodGroup bloodGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EmployeeStatus status;

    @Column(name = "created_on")
    private ZonedDateTime createdOn;

    @Column(name = "last_updated_on")
    private ZonedDateTime lastUpdatedOn;
}
