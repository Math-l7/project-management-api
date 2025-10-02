package project_management_api.project_management_api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import project_management_api.project_management_api.dto.TaskInputDTO;
import project_management_api.project_management_api.dto.TaskReturnDTO;
import project_management_api.project_management_api.dto.TaskUpdateDTO;
import project_management_api.project_management_api.enums.TaskStatus;
import project_management_api.project_management_api.service.TaskService;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/projects/{projectId}")
    public ResponseEntity<TaskReturnDTO> createTask(@RequestBody TaskInputDTO task, @PathVariable Integer projectId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(task, projectId));
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskReturnDTO> updateTask(@PathVariable Integer taskId, @RequestBody TaskUpdateDTO newTask) {
        return ResponseEntity.ok(taskService.updateTask(taskId, newTask));
    }

    @PutMapping("/{taskId}/status")
    public ResponseEntity<TaskReturnDTO> changeTaskStatus(@PathVariable Integer taskId,
            @RequestBody TaskStatus status) {
        return ResponseEntity.ok(taskService.changeTaskStatus(taskId, status));
    }

    @GetMapping("/projects/{projectId}")
    public ResponseEntity<List<TaskReturnDTO>> getTasksByProject(@PathVariable Integer projectId) {
        return ResponseEntity.ok(taskService.getTasksByProject(projectId));
    }

    @GetMapping("/users/me")
    public ResponseEntity<List<TaskReturnDTO>> getTasksByUser() {
        return ResponseEntity.ok(taskService.getTasksByUser());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TaskReturnDTO>> getTasksByUserId(@PathVariable Integer userId) {
        return ResponseEntity.ok(taskService.getTasksByUserId(userId));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Integer taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

}
