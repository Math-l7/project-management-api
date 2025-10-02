package project_management_api.project_management_api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

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
import project_management_api.project_management_api.repository.UserRepository;
import project_management_api.project_management_api.service.NotificationService;
import project_management_api.project_management_api.service.TaskService;
import project_management_api.project_management_api.service.UserService;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    private User taskOwner;
    private Project project;
    private Task task;
    private TaskInputDTO taskInput;
    private TaskUpdateDTO taskUpdate;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserService userService;

    @InjectMocks
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        // Usuário dono da task
        taskOwner = new User();
        taskOwner.setId(1);
        taskOwner.setName("Owner");
        taskOwner.setEmail("owner@email.com");
        taskOwner.setTasks(new ArrayList<>());

        // Projeto
        project = new Project();
        project.setId(1);
        project.setName("Projeto Teste");
        project.setTasks(new ArrayList<>());
        project.setUsers(new ArrayList<>());
        project.getUsers().add(taskOwner);

        // Task
        task = new Task();
        task.setId(1);
        task.setTitle("Task");
        task.setDescription("Descrição");
        task.setTaskOwner(taskOwner);
        task.setProjectOwner(project);
        task.setStatus(TaskStatus.TO_DO);

        // DTO para criação
        taskInput = new TaskInputDTO();
        taskInput.setTitle(task.getTitle());
        taskInput.setDescription(task.getDescription());

        // DTO para atualização
        taskUpdate = new TaskUpdateDTO();
        taskUpdate.setTitle("Task Atualizada");
        taskUpdate.setDescription("Nova descrição");
        taskUpdate.setTaskOwnerId(2);

        // adiciona vínculos
        project.getTasks().add(task);
        taskOwner.getTasks().add(task);
    }

    @Test
    public void createTask_Success() {
        when(taskRepository.existsByTitleAndProjectOwnerId(taskInput.getTitle(), project.getId())).thenReturn(false);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(userService.getAuthenticatedUser()).thenReturn(taskOwner);

        TaskReturnDTO result = taskService.createTask(taskInput, project.getId());

        assertEquals("Task", result.getTitle());
        assertEquals("Descrição", result.getDescription());
        assertEquals(project.getId(), result.getProjectId());
        assertEquals(taskOwner.getId(), result.getTaskOwnerId());

        verify(taskRepository).save(any(Task.class));
        verify(notificationService).sendNotificationToProject(any(NotificationInputDTOToProject.class));
        verify(notificationService).sendNotificationToUser(any(NotificationInputDTOToUser.class));
    }

    @Test
    public void createTask_WhenTaskAlreadyExists() {
        when(taskRepository.existsByTitleAndProjectOwnerId(taskInput.getTitle(), project.getId())).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> taskService.createTask(taskInput, project.getId()));
    }

    @Test
    public void createTask_WhenProjectNotFound() {
        when(taskRepository.existsByTitleAndProjectOwnerId(taskInput.getTitle(), project.getId())).thenReturn(false);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> taskService.createTask(taskInput, project.getId()));
    }

    @Test
    public void createTask_WhenUserNotInProject() {
        User outsider = new User();
        outsider.setId(99);
        when(taskRepository.existsByTitleAndProjectOwnerId(taskInput.getTitle(), project.getId())).thenReturn(false);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(userService.getAuthenticatedUser()).thenReturn(outsider);

        assertThrows(ResponseStatusException.class, () -> taskService.createTask(taskInput, project.getId()));
    }

    @Test
    public void updateTask_Success() {
        User newOwner = new User();
        newOwner.setId(2);
        project.getUsers().add(newOwner);

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(userService.getAuthenticatedUser()).thenReturn(taskOwner);
        when(userService.findUserByIdOrThrow(newOwner.getId())).thenReturn(newOwner);

        TaskReturnDTO result = taskService.updateTask(task.getId(), taskUpdate);

        assertEquals(taskUpdate.getTitle(), result.getTitle());
        assertEquals(taskUpdate.getDescription(), result.getDescription());
        assertEquals(taskUpdate.getTaskOwnerId(), result.getTaskOwnerId());

        verify(taskRepository).save(any(Task.class));
        verify(notificationService).sendNotificationToUser(any(NotificationInputDTOToUser.class));
        verify(notificationService).sendNotificationToProject(any(NotificationInputDTOToProject.class));
    }

    @Test
    public void updateTask_WhenTaskNotFound() {
        when(taskRepository.findById(task.getId())).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> taskService.updateTask(task.getId(), taskUpdate));
    }

    @Test
    public void updateTask_WhenUserNotInProject() {
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        User outsider = new User();
        outsider.setId(99);
        when(userService.getAuthenticatedUser()).thenReturn(outsider);

        assertThrows(ResponseStatusException.class, () -> taskService.updateTask(task.getId(), taskUpdate));
    }

    @Test
    public void changeTaskStatus_Success() {
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(userService.getAuthenticatedUser()).thenReturn(taskOwner);

        TaskReturnDTO result = taskService.changeTaskStatus(task.getId(), TaskStatus.DONE);

        assertEquals(TaskStatus.DONE.toString(), result.getStatus());

        verify(taskRepository).save(any(Task.class));
        verify(notificationService).sendNotificationToProject(any(NotificationInputDTOToProject.class));
    }

    @Test
    public void changeTaskStatus_WhenTaskNotFound() {
        when(taskRepository.findById(task.getId())).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> taskService.changeTaskStatus(task.getId(), TaskStatus.DONE));
    }

    @Test
    public void changeTaskStatus_WhenStatusAlreadySet() {
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(userService.getAuthenticatedUser()).thenReturn(taskOwner);

        assertThrows(ResponseStatusException.class, () -> taskService.changeTaskStatus(task.getId(), TaskStatus.TO_DO));
    }

    @Test
    public void getTasksByProject_Success() {
        Task newTask = new Task();
        newTask.setId(2);
        newTask.setTitle("New Title");
        newTask.setDescription("Description two");
        newTask.setProjectOwner(project);

        project.setTasks(List.of(task, newTask));

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(userService.getAuthenticatedUser()).thenReturn(taskOwner);
        when(taskRepository.findByProjectOwnerId(project.getId())).thenReturn(project.getTasks());

        List<TaskReturnDTO> tasks = taskService.getTasksByProject(project.getId());

        assertEquals(2, tasks.size());
        assertEquals("Task", tasks.get(0).getTitle());
        assertEquals("New Title", tasks.get(1).getTitle());
    }

    @Test
    public void getTasksByProject_WhenProjectNotFound() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> taskService.getTasksByProject(project.getId()));
    }

    @Test
    public void getTasksByProject_WhenUserNotInProject() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        User outsider = new User();
        outsider.setId(99);
        when(userService.getAuthenticatedUser()).thenReturn(outsider);

        assertThrows(ResponseStatusException.class, () -> taskService.getTasksByProject(project.getId()));
    }

    @Test
    public void getTasksByUser_Success() {
        Task newTask = new Task();
        newTask.setId(2);
        newTask.setTitle("New Title");
        newTask.setDescription("Description two");
        newTask.setProjectOwner(project);

        when(userService.getAuthenticatedUser()).thenReturn(taskOwner);
        when(taskRepository.findByTaskOwnerId(taskOwner.getId())).thenReturn(List.of(task, newTask));

        List<TaskReturnDTO> tasks = taskService.getTasksByUser();

        assertEquals(2, tasks.size());
        assertEquals("Task", tasks.get(0).getTitle());
        assertEquals("New Title", tasks.get(1).getTitle());
    }

    @Test
    public void getTasksByUserId_Success() {
        Task newTask = new Task();
        newTask.setId(2);
        newTask.setTitle("New Title");
        newTask.setDescription("Description two");
        newTask.setProjectOwner(project);

        when(userService.findUserByIdOrThrow(taskOwner.getId())).thenReturn(taskOwner);
        when(taskRepository.findByTaskOwnerId(taskOwner.getId())).thenReturn(List.of(task, newTask));

        List<TaskReturnDTO> tasks = taskService.getTasksByUserId(taskOwner.getId());

        assertEquals(2, tasks.size());
        assertEquals("Task", tasks.get(0).getTitle());
        assertEquals("New Title", tasks.get(1).getTitle());
    }

    @Test
    public void deleteTask_Success() {
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        taskService.deleteTask(task.getId());

        verify(taskRepository).delete(task);
        verify(notificationService).sendNotificationToProject(any(NotificationInputDTOToProject.class));
    }

    @Test
    public void deleteTask_WhenTaskNotFound() {
        when(taskRepository.findById(task.getId())).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> taskService.deleteTask(task.getId()));
    }
}