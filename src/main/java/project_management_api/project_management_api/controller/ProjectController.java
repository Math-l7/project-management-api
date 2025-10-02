package project_management_api.project_management_api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import project_management_api.project_management_api.dto.ProjectInputDTO;
import project_management_api.project_management_api.dto.ProjectReturnDTO;
import project_management_api.project_management_api.dto.ProjectUpdateDTO;
import project_management_api.project_management_api.enums.ProjectStatus;
import project_management_api.project_management_api.service.ProjectService;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectReturnDTO> createProject(@RequestBody ProjectInputDTO projectDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(projectDto));
    }

    @PutMapping("/{projectId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectReturnDTO> updateProject(@RequestBody ProjectUpdateDTO projectUpdateDTO,
            @PathVariable Integer projectId) {
        return ResponseEntity.ok(projectService.updateProject(projectUpdateDTO, projectId));
    }

    @PutMapping("/{projectId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectReturnDTO> updateStatusProject(@PathVariable Integer projectId,
            @RequestParam ProjectStatus status) {
        return ResponseEntity.ok(projectService.updateStatusProject(projectId, status));
    }

    @GetMapping("/me")
    public ResponseEntity<List<ProjectReturnDTO>> getMyProjects() {
        return ResponseEntity.ok(projectService.getProjectsByUser(null));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProjectReturnDTO>> getProjectsByUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(projectService.getProjectsByUser(userId));
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectReturnDTO> getProjectById(@PathVariable Integer projectId) {
        return ResponseEntity.ok(projectService.getProjectById(projectId));
    }

    @PutMapping("/{projectId}/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectReturnDTO> addUserToProject(@PathVariable Integer projectId,
            @PathVariable Integer userId) {
        return ResponseEntity.ok(projectService.addUserToProject(projectId, userId));
    }

    @DeleteMapping("/{projectId}/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeUserFromProject(@PathVariable Integer projectId,
            @PathVariable Integer userId) {
        projectService.removeUserFromProject(projectId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{projectId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProject(@PathVariable Integer projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.noContent().build();
    }

}
