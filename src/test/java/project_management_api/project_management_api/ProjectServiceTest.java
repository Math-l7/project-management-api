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
import project_management_api.project_management_api.dto.ProjectInputDTO;
import project_management_api.project_management_api.dto.ProjectReturnDTO;
import project_management_api.project_management_api.dto.ProjectUpdateDTO;
import project_management_api.project_management_api.enums.ProjectStatus;
import project_management_api.project_management_api.enums.RoleName;
import project_management_api.project_management_api.model.Project;
import project_management_api.project_management_api.model.User;
import project_management_api.project_management_api.repository.ProjectRepository;
import project_management_api.project_management_api.repository.UserRepository;
import project_management_api.project_management_api.service.NotificationService;
import project_management_api.project_management_api.service.ProjectService;
import project_management_api.project_management_api.service.UserService;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

    private User userCreator;
    private User anotherUser;
    private Project project;
    private ProjectInputDTO projectInput;
    private ProjectUpdateDTO projectUpdate;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        userCreator = new User();
        userCreator.setId(1);
        userCreator.setName("Creator");
        userCreator.setEmail("creator@email.com");
        userCreator.setProjects(new ArrayList<>());
        userCreator.setRole(RoleName.ROLE_ADMIN);

        project = new Project();
        project.setId(1);
        project.setName("Projeto Teste");
        project.setDescription("Descrição Teste");
        project.setStatus(ProjectStatus.ACTIVE);
        project.setUsers(new ArrayList<>());
        project.getUsers().add(userCreator);
        userCreator.getProjects().add(project);

        projectInput = new ProjectInputDTO();
        projectInput.setName(project.getName());
        projectInput.setDescription(project.getDescription());

        projectUpdate = new ProjectUpdateDTO();
        projectUpdate.setName("Projeto Atualizado");
        projectUpdate.setDescription("Nova descrição");

        anotherUser = new User();
        anotherUser.setId(2);
        anotherUser.setName("Another User");
        anotherUser.setEmail("another@email.com");
        anotherUser.setProjects(new ArrayList<>());
        anotherUser.setRole(RoleName.ROLE_USER);
    }

    @Test
    public void createProject_Success() {
        when(projectRepository.existsByName(projectInput.getName())).thenReturn(false);
        when(userService.getAuthenticatedUser()).thenReturn(userCreator);

        ProjectReturnDTO result = projectService.createProject(projectInput);

        assertEquals("Projeto Teste", result.getName());
        assertEquals("Descrição Teste", result.getDescription());

        verify(projectRepository).save(any(Project.class));
        verify(notificationService).sendNotificationToUser(any(NotificationInputDTOToUser.class));
    }

    @Test
    public void createProject_WhenNameAlreadyExists() {
        when(projectRepository.existsByName(projectInput.getName())).thenReturn(true);

        assertThrows(ResponseStatusException.class,
                () -> projectService.createProject(projectInput));
    }

    @Test
    public void updateProject_Success() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(userService.getAuthenticatedUser()).thenReturn(userCreator);

        ProjectReturnDTO result = projectService.updateProject(projectUpdate, project.getId());

        assertEquals("Projeto Atualizado", result.getName());
        assertEquals("Nova descrição", result.getDescription());

        verify(projectRepository).save(any(Project.class));
        verify(notificationService).sendNotificationToProject(any(NotificationInputDTOToProject.class));
    }

    @Test
    public void updateProject_WhenProjectNotFound() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> projectService.updateProject(projectUpdate, project.getId()));
    }

    @Test
    public void updateStatusProject_Success() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(userService.getAuthenticatedUser()).thenReturn(userCreator);

        ProjectReturnDTO result = projectService.updateStatusProject(project.getId(),
                ProjectStatus.COMPLETED);

        assertEquals(ProjectStatus.COMPLETED.toString(), result.getStatus());

        verify(projectRepository).save(any(Project.class));
        verify(notificationService).sendNotificationToProject(any(NotificationInputDTOToProject.class));
    }

    @Test
    public void updateStatusProject_WhenProjectNotFound() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> projectService.updateStatusProject(project.getId(), ProjectStatus.COMPLETED));
    }

    @Test
    public void updateStatusProject_WhenStatusAlreadySet() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(userService.getAuthenticatedUser()).thenReturn(userCreator);

        project.setStatus(ProjectStatus.COMPLETED);

        assertThrows(ResponseStatusException.class,
                () -> projectService.updateStatusProject(project.getId(), ProjectStatus.COMPLETED));
    }

    @Test
    public void getProjectsByUser_Success_Admin() {
        when(userService.getAuthenticatedUser()).thenReturn(userCreator);
        when(projectRepository.findByUsers_Id(userCreator.getId())).thenReturn(List.of(project));

        List<ProjectReturnDTO> projects = projectService.getProjectsByUser(userCreator.getId());

        assertEquals(1, projects.size());
        assertEquals("Projeto Teste", projects.get(0).getName());
    }

    @Test
    public void getProjectsByUser_WhenNotAdminAccessDenied() {
        when(userService.getAuthenticatedUser()).thenReturn(anotherUser);

        assertThrows(ResponseStatusException.class,
                () -> projectService.getProjectsByUser(userCreator.getId()));
    }

    @Test
    public void getProjectById_Success_Admin() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(userService.getAuthenticatedUser()).thenReturn(userCreator);

        ProjectReturnDTO result = projectService.getProjectById(project.getId());

        assertEquals("Projeto Teste", result.getName());
    }

    @Test
    public void getProjectById_WhenProjectNotFound() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> projectService.getProjectById(project.getId()));
    }

    @Test
    public void getProjectById_WhenUserNotInProject() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(userService.getAuthenticatedUser()).thenReturn(anotherUser);

        assertThrows(ResponseStatusException.class,
                () -> projectService.getProjectById(project.getId()));
    }

    @Test
    public void addUserToProject_Success() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(userRepository.findById(anotherUser.getId())).thenReturn(Optional.of(anotherUser));

        ProjectReturnDTO result = projectService.addUserToProject(project.getId(), anotherUser.getId());

        assertEquals(2, project.getUsers().size());
        assertEquals("Another User", project.getUsers().get(1).getName());

        verify(projectRepository).save(any(Project.class));
        verify(notificationService).sendNotificationToProject(any(NotificationInputDTOToProject.class));
    }

    @Test
    public void addUserToProject_WhenProjectNotFound() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> projectService.addUserToProject(project.getId(), anotherUser.getId()));
    }

    @Test
    public void addUserToProject_WhenUserNotFound() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(userRepository.findById(anotherUser.getId())).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> projectService.addUserToProject(project.getId(), anotherUser.getId()));
    }

    @Test
    public void removeUserFromProject_Success() {
        project.getUsers().add(anotherUser);
        anotherUser.getProjects().add(project);

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(userRepository.findById(anotherUser.getId())).thenReturn(Optional.of(anotherUser));

        ProjectReturnDTO result = projectService.removeUserFromProject(project.getId(), anotherUser.getId());

        assertEquals(1, project.getUsers().size());
        assertEquals("Creator", project.getUsers().get(0).getName());

        verify(projectRepository).save(any(Project.class));
        verify(notificationService).sendNotificationToProject(any(NotificationInputDTOToProject.class));
    }

    @Test
    public void removeUserFromProject_WhenProjectNotFound() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> projectService.removeUserFromProject(project.getId(), anotherUser.getId()));
    }

    @Test
    public void removeUserFromProject_WhenUserNotFound() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(userRepository.findById(anotherUser.getId())).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> projectService.removeUserFromProject(project.getId(), anotherUser.getId()));
    }

    @Test
    public void removeUserFromProject_WhenUserNotInProject() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(userRepository.findById(anotherUser.getId())).thenReturn(Optional.of(anotherUser));

        assertThrows(ResponseStatusException.class,
                () -> projectService.removeUserFromProject(project.getId(), anotherUser.getId()));
    }

    @Test
    public void deleteProject_Success() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

        projectService.deleteProject(project.getId());

        verify(projectRepository).delete(any(Project.class));
        verify(notificationService).sendNotificationToUser(any(NotificationInputDTOToUser.class));
    }

    @Test
    public void deleteProject_WhenProjectNotFound() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> projectService.deleteProject(project.getId()));
    }
}