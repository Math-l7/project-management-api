package project_management_api.project_management_api.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;
import project_management_api.project_management_api.enums.ProjectStatus;

@Entity
@Getter
@Setter
public class Project {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "VARCHAR DEFAULT 'Nenhuma descrição atribuída a este projeto.'")
    private String description;

    @Enumerated(EnumType.STRING)
    private ProjectStatus status = ProjectStatus.ACTIVE;

    // relacionamento com users (MEMBROS) (FK) many to many
    @ManyToMany
    @JoinTable(name = "user_project", joinColumns = @JoinColumn(name = "project_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> users;

    // relacionamento com tasks (FK) one to many
    @OneToMany(mappedBy = "projectOwner")
    private List<Task> tasks;

    @OneToMany(mappedBy = "project")
    private List<Message> messages = new ArrayList<>();

}
