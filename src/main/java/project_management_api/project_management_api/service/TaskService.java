package project_management_api.project_management_api.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.transaction.Transactional;
import project_management_api.project_management_api.dto.NotificationInputDTOToProject;
import project_management_api.project_management_api.dto.NotificationInputDTOToUser;
import project_management_api.project_management_api.dto.TaskInputDTO;
import project_management_api.project_management_api.dto.TaskReturnDTO;
import project_management_api.project_management_api.dto.TaskUpdateDTO;
import project_management_api.project_management_api.enums.TaskStatus;
import project_management_api.project_management_api.model.Project;
import project_management_api.project_management_api.model.Task;
import project_management_api.project_management_api.model.User;
import project_management_api.project_management_api.repository.ProjectRepository;
import project_management_api.project_management_api.repository.TaskRepository;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final NotificationService notificationService;
    private final UserService userService;

    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository,
            NotificationService notificationService, UserService userService) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.notificationService = notificationService;
        this.userService = userService;
    }

    private TaskReturnDTO toTaskDTO(Task task) {
        return new TaskReturnDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus().name(),
                task.getProjectOwner() != null ? task.getProjectOwner().getId() : null,
                task.getTaskOwner() != null ? task.getTaskOwner().getId() : null);
    }

    private Project findProjectByIdOrThrow(Integer projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Projeto não encontrado"));
    }

    private Task findTaskByIdOrThrow(Integer taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task não encontrada"));
    }

    //
    @Transactional
    public TaskReturnDTO createTask(TaskInputDTO taskDto, Integer projectId) {

        if (taskRepository.existsByTitleAndProjectOwnerId(taskDto.getTitle(), projectId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Já existe uma task com esse título nesse projeto.");
        }

        Project project = findProjectByIdOrThrow(projectId);

        User taskOwner = userService.getAuthenticatedUser();

        if (!project.getUsers().contains(taskOwner)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário não pertence ao projeto.");
        }

        Task task = new Task();
        task.setTitle(taskDto.getTitle());
        task.setDescription(taskDto.getDescription());
        task.setProjectOwner(project);
        task.setTaskOwner(taskOwner);

        taskRepository.save(task);

        // 1. Notificação para TODOS os membros do projeto (Visibilidade)
        notificationService.sendNotificationToProject(new NotificationInputDTOToProject(
                "Nova Task criada: '" + task.getTitle() + "' no projeto " + project.getName(),
                projectId));

        // 2. Notificação Específica para o Responsável (Responsabilidade)
        if (task.getTaskOwner() != null) {
            notificationService.sendNotificationToUser(new NotificationInputDTOToUser(
                    "Você foi atribuído à nova task: '" + task.getTitle() + "'",
                    task.getTaskOwner().getId()));
        }

        return toTaskDTO(task);
    }

    @Transactional
    public TaskReturnDTO updateTask(Integer taskId, TaskUpdateDTO newTask) {

        Task task = findTaskByIdOrThrow(taskId);

        User user = userService.getAuthenticatedUser();

        if (!task.getProjectOwner().getUsers().contains(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para editar essa task.");
        }

        if (newTask.getTitle() != null && !task.getTitle().equals(newTask.getTitle())) {
            task.setTitle(newTask.getTitle());
        }
        if (newTask.getDescription() != null && !task.getDescription().equals(newTask.getDescription())) {
            task.setDescription(newTask.getDescription());
        }

        if (newTask.getTaskOwnerId() != null &&
                (task.getTaskOwner() == null || !task.getTaskOwner().getId().equals(newTask.getTaskOwnerId()))) {
            User newOwner = userService.findUserByIdOrThrow(newTask.getTaskOwnerId());

            if (!task.getProjectOwner().getUsers().contains(newOwner)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Novo dono não pertence ao projeto.");
            }
            notificationService.sendNotificationToUser(new NotificationInputDTOToUser(
                    "Você foi reatribuído à task '" + task.getTitle() + "'.",
                    newOwner.getId()));

            task.setTaskOwner(newOwner);
        }

        taskRepository.save(task);
        notificationService.sendNotificationToProject(new NotificationInputDTOToProject(
                "Task '" + task.getTitle() + "' foi atualizada no projeto " + task.getProjectOwner().getName(),
                task.getProjectOwner().getId()));

        return toTaskDTO(task);

    }

    @Transactional
    public TaskReturnDTO changeTaskStatus(Integer taskId, TaskStatus status) {

        Task task = findTaskByIdOrThrow(taskId);

        User user = userService.getAuthenticatedUser();

        if (!task.getProjectOwner().getUsers().contains(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para editar essa task.");
        }

        if (task.getStatus().equals(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status já atribuído a essa task.");
        }

        task.setStatus(status);
        taskRepository.save(task);
        notificationService.sendNotificationToProject(new NotificationInputDTOToProject(
                "Status da task '" + task.getTitle() + "' foi atualizado para " + status.toString(),
                task.getProjectOwner().getId()));
        return toTaskDTO(task);

    }

    public List<TaskReturnDTO> getTasksByProject(Integer projectId) {

        Project project = findProjectByIdOrThrow(projectId);

        User user = userService.getAuthenticatedUser();

        if (!project.getUsers().contains(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem acesso a este projeto.");
        }

        return taskRepository.findByProjectOwnerId(projectId)
                .stream()
                .map(task -> {
                    return new TaskReturnDTO(
                            task.getId(),
                            task.getTitle(),
                            task.getDescription(),
                            task.getStatus().name(),
                            task.getProjectOwner() != null ? task.getProjectOwner().getId() : null,
                            task.getTaskOwner() != null ? task.getTaskOwner().getId() : null);
                })
                .toList();
    }

    public List<TaskReturnDTO> getTasksByUser() {
        User user = userService.getAuthenticatedUser();

        return taskRepository.findByTaskOwnerId(user.getId())
                .stream()
                .map(task -> {
                    return new TaskReturnDTO(
                            task.getId(),
                            task.getTitle(),
                            task.getDescription(),
                            task.getStatus().name(),
                            task.getProjectOwner() != null ? task.getProjectOwner().getId() : null,
                            task.getTaskOwner() != null ? task.getTaskOwner().getId() : null);
                })
                .toList();

    }

    public List<TaskReturnDTO> getTasksByUserId(Integer userId) {
        User user = userService.findUserByIdOrThrow(userId);

        return taskRepository.findByTaskOwnerId(user.getId())
                .stream()
                .map(task -> {
                    return new TaskReturnDTO(
                            task.getId(),
                            task.getTitle(),
                            task.getDescription(),
                            task.getStatus().name(),
                            task.getProjectOwner() != null ? task.getProjectOwner().getId() : null,
                            task.getTaskOwner() != null ? task.getTaskOwner().getId() : null);
                })
                .toList();

    }

    @Transactional
    public void deleteTask(Integer idTask) {
        Task task = findTaskByIdOrThrow(idTask);
        Project project = task.getProjectOwner();

        notificationService.sendNotificationToProject(new NotificationInputDTOToProject(
                "Task '" + task.getTitle() + "' foi permanentemente excluída do projeto " + project.getName(),
                project.getId()));

        taskRepository.delete(task);
    }

}
