package org.example.kpiservice.secondary.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.kpiservice.secondary.enums.ActiveStatus;
import org.example.kpiservice.secondary.enums.TeamStatus;

import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "team")
public class Team {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "firebase_topic")
    private String firebaseTopic;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TeamStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "active_status")
    private ActiveStatus activeStatus;

    @Column(name = "created_on")
    private ZonedDateTime createdOn;

    @Column(name = "last_updated_on")
    private ZonedDateTime lastUpdatedOn;
}
