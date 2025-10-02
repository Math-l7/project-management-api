package project_management_api.project_management_api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import project_management_api.project_management_api.enums.TaskStatus;

@Entity
@Getter
@Setter
public class Task {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Integer id;

    @Column(nullable = false, unique = true)
    private String title;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.TO_DO;

    // relacionamento project(FK) many to one
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project projectOwner;

    @ManyToOne
    @JoinColumn(name = "task_owner_id")
    private User taskOwner;

}
