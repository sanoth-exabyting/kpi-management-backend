package org.example.kpiservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.kpiservice.enums.BloodGroup;
import org.example.kpiservice.enums.EmployeeStatus;
import org.example.kpiservice.enums.Gender;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@Entity
@Table(name = "employees")
public class Employee extends BaseEntity {

    @Column(name = "employee_id", nullable = false, unique = true, length = 50)
    private String employeeId;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "partner_id")
    private Long partnerId;

    @Column(name = "supervisor_id", length = 50)
    private String supervisorId;

    @Column(name = "date_of_birth")
    private Instant dateOfBirth;

    @Column(name = "joining_date")
    private Instant joiningDate;

    @Column(name = "designation", length = 255)
    private String designation;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 20)
    private Gender gender;

    @Column(name = "nid", length = 50)
    private String nid;

    @Column(name = "tin_number", length = 50)
    private String tinNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_group", length = 20)
    private BloodGroup bloodGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EmployeeStatus status;
}
