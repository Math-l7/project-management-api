package project_management_api.project_management_api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import project_management_api.project_management_api.controller.SseController;
import project_management_api.project_management_api.dto.NotificationInputDTOToProject;
import project_management_api.project_management_api.dto.NotificationInputDTOToUser;
import project_management_api.project_management_api.dto.NotificationReturnDTO;
import project_management_api.project_management_api.dto.NotificationReturnDTOTProject;
import project_management_api.project_management_api.enums.NotificationStatus;
import project_management_api.project_management_api.enums.ProjectStatus;
import project_management_api.project_management_api.model.Notification;
import project_management_api.project_management_api.model.Project;
import project_management_api.project_management_api.model.User;
import project_management_api.project_management_api.repository.NotificationRepository;
import project_management_api.project_management_api.repository.ProjectRepository;
import project_management_api.project_management_api.repository.UserRepository;
import project_management_api.project_management_api.service.NotificationService;
import project_management_api.project_management_api.service.UserService;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    private User user;
    private User anotherUser;
    private Project project;
    private Notification notification;
    private Notification anotherNotification;
    private NotificationInputDTOToProject notificationToProject;
    private NotificationInputDTOToUser notificationToUser;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SseController sseController;

    @Mock
    private UserService userService;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        // Usuário principal
        user = new User();
        user.setId(1);
        user.setName("Matheus");

        anotherUser = new User();
        anotherUser.setId(2);
        anotherUser.setName("Miguel");

        // Projeto
        project = new Project();
        project.setId(10);
        project.setName("Projeto Teste");
        project.setStatus(ProjectStatus.ACTIVE);
        project.getUsers().add(user);
        project.getUsers().add(anotherUser);

        // Notificações
        notification = new Notification();
        notification.setId(100);
        notification.setTextNotification("Notificação inicial");
        notification.setTime(LocalDateTime.now());
        notification.setStatus(NotificationStatus.NOT_READ);
        notification.setUserDestin(user);

        anotherNotification = new Notification();
        anotherNotification.setId(101);
        anotherNotification.setTextNotification("Outra notificação");
        anotherNotification.setTime(LocalDateTime.now());
        anotherNotification.setStatus(NotificationStatus.NOT_READ);
        anotherNotification.setUserDestin(user);

        // DTOs
        notificationToProject = new NotificationInputDTOToProject();
        notificationToProject.setProjectId(project.getId());
        notificationToProject.setTextNotification("Mensagem para o projeto");

        notificationToUser = new NotificationInputDTOToUser();
        notificationToUser.setUserDestinId(user.getId());
        notificationToUser.setTextNotification("Mensagem direta para o usuário");

        // Simula que o usuário já tem notificações
        user.getNotifications().add(notification);
        user.getNotifications().add(anotherNotification);
    }

    @Test
    public void sendNotificationToProject_Success() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

        NotificationReturnDTOTProject result = notificationService.sendNotificationToProject(notificationToProject);

        assertEquals(notificationToProject.getTextNotification(), result.getTextNotification());
        verify(notificationRepository).saveAll(anyList());
        verify(projectRepository).findById(project.getId());
        verify(sseController).sendNotification(anyString());
    }

    @Test
    public void sendNotificationToProject_WhenProjectNotFound() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> notificationService.sendNotificationToProject(notificationToProject));
    }

    @Test
    public void sendNotificationToUser_Success() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        NotificationReturnDTO result = notificationService.sendNotificationToUser(notificationToUser);

        assertEquals(notificationToUser.getTextNotification(), result.getTextNotification());
        verify(notificationRepository).save(any(Notification.class));
        verify(userRepository).findById(user.getId());
        verify(sseController).sendNotification(anyString());
    }

    @Test
    public void sendNotificationToUser_WhenUserNotFound() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> notificationService.sendNotificationToUser(notificationToUser));
    }

    @Test
    public void markRead_Success() {
        when(userService.getAuthenticatedUser()).thenReturn(user);
        when(notificationRepository.findById(notification.getId())).thenReturn(Optional.of(notification));

        NotificationReturnDTO result = notificationService.markRead(notification.getId());

        assertEquals(NotificationStatus.READ.toString(), result.getStatus());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    public void markRead_WhenNotificationNotFound() {
        when(userService.getAuthenticatedUser()).thenReturn(user);
        when(notificationRepository.findById(notification.getId())).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> notificationService.markRead(notification.getId()));
    }

    @Test
    public void markRead_WhenNotificationAlreadyRead() {
        when(userService.getAuthenticatedUser()).thenReturn(user);
        notification.setStatus(NotificationStatus.READ);
        when(notificationRepository.findById(notification.getId())).thenReturn(Optional.of(notification));

        assertThrows(ResponseStatusException.class, () -> notificationService.markRead(notification.getId()));
    }

    @Test
    public void markRead_WhenUserNotOwner() {
        when(userService.getAuthenticatedUser()).thenReturn(anotherUser);
        when(notificationRepository.findById(notification.getId())).thenReturn(Optional.of(notification));

        assertThrows(ResponseStatusException.class, () -> notificationService.markRead(notification.getId()));
    }

    @Test
    public void markAllRead_Success() {
        when(userService.getAuthenticatedUser()).thenReturn(user);

        notification.setStatus(NotificationStatus.NOT_READ);
        anotherNotification.setStatus(NotificationStatus.NOT_READ);

        user.setNotifications(List.of(notification, anotherNotification));

        List<NotificationReturnDTO> result = notificationService.markAllRead();

        assertEquals(2, result.size());
        assertEquals(NotificationStatus.READ.toString(), result.get(0).getStatus());
        assertEquals(NotificationStatus.READ.toString(), result.get(1).getStatus());
        verify(notificationRepository).saveAll(anyList());
    }

    @Test
    public void getNotificationsByUser_Success() {
        when(userService.getAuthenticatedUser()).thenReturn(user);

        user.setNotifications(List.of(notification, anotherNotification));

        List<NotificationReturnDTO> result = notificationService.getNotificationsByUser();

        assertEquals(2, result.size());
        assertEquals(notification.getTextNotification(), result.get(0).getTextNotification());
        assertEquals(anotherNotification.getTextNotification(), result.get(1).getTextNotification());
    }

    @Test
    public void getNotificationsNotRead_Success() {
        when(userService.getAuthenticatedUser()).thenReturn(user);

        notification.setStatus(NotificationStatus.NOT_READ);
        anotherNotification.setStatus(NotificationStatus.NOT_READ);
        user.setNotifications(List.of(notification, anotherNotification));

        List<NotificationReturnDTO> result = notificationService.getNotificationsNotRead();

        assertEquals(2, result.size());
        assertEquals(notification.getTextNotification(), result.get(0).getTextNotification());
        assertEquals(anotherNotification.getTextNotification(), result.get(1).getTextNotification());
    }

    @Test
    public void deleteNotification_Success() {
        when(notificationRepository.findById(notification.getId())).thenReturn(Optional.of(notification));

        notificationService.deleteNotification(notification.getId());

        verify(notificationRepository).delete(any(Notification.class));
    }

    @Test
    public void deleteNotification_WhenNotificationNotFound() {
        when(notificationRepository.findById(notification.getId())).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> notificationService.deleteNotification(notification.getId()));
    }
}