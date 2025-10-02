package project_management_api.project_management_api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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

import project_management_api.project_management_api.dto.MessageInputDTO;
import project_management_api.project_management_api.dto.MessageReturnDTO;
import project_management_api.project_management_api.dto.NotificationInputDTOToProject;
import project_management_api.project_management_api.enums.MessageStatus;
import project_management_api.project_management_api.enums.RoleName;
import project_management_api.project_management_api.model.Message;
import project_management_api.project_management_api.model.Project;
import project_management_api.project_management_api.model.User;
import project_management_api.project_management_api.repository.MessageRepository;
import project_management_api.project_management_api.repository.ProjectRepository;
import project_management_api.project_management_api.repository.UserRepository;
import project_management_api.project_management_api.service.MessageService;
import project_management_api.project_management_api.service.NotificationService;
import project_management_api.project_management_api.service.UserService;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserService userService;

    @InjectMocks
    private MessageService messageService;

    private User user;
    private Project project;
    private Message message;
    private MessageInputDTO messageInputDTO;
    private User anotherUser;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);
        user.setName("Matheus");
        user.setRole(RoleName.ROLE_USER);

        anotherUser = new User();
        anotherUser.setId(2);
        anotherUser.setName("Miguel");
        anotherUser.setRole(RoleName.ROLE_USER);

        project = new Project();
        project.setId(10);
        project.setName("Projeto Teste");
        project.getUsers().add(user);

        message = new Message();
        message.setId(100);
        message.setText("Mensagem inicial");
        message.setTime(LocalDateTime.now());
        message.setStatus(MessageStatus.NOT_READ);
        message.setUser(user);
        message.setProject(project);

        messageInputDTO = new MessageInputDTO();
        messageInputDTO.setText("Mensagem DTO de teste");
    }

    @Test
    public void sendMessage_Success() {
        when(userService.getAuthenticatedUser()).thenReturn(user);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

        MessageReturnDTO result = messageService.sendMessage(project.getId(), messageInputDTO);

        verify(messageRepository).save(any(Message.class));
        verify(notificationService).sendNotificationToProject(any(NotificationInputDTOToProject.class));
        assertEquals(messageInputDTO.getText(), result.getText());
    }

    @Test
    public void sendMessage_WhenProjectNotFound() {
        when(userService.getAuthenticatedUser()).thenReturn(user);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> messageService.sendMessage(project.getId(), messageInputDTO));
    }

    @Test
    public void sendMessage_WhenUserNotInProject() {
        when(userService.getAuthenticatedUser()).thenReturn(anotherUser);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

        assertThrows(ResponseStatusException.class,
                () -> messageService.sendMessage(project.getId(), messageInputDTO));
    }

    @Test
    public void markRead_Success() {
        when(messageRepository.findById(message.getId())).thenReturn(Optional.of(message));
        when(userService.getAuthenticatedUser()).thenReturn(user);

        MessageReturnDTO result = messageService.markRead(message.getId());

        assertNotNull(result);
        assertEquals(MessageStatus.READ, result.getStatus());
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    public void markRead_WhenAlreadyMarkedRead() {
        message.setStatus(MessageStatus.READ);
        when(messageRepository.findById(message.getId())).thenReturn(Optional.of(message));
        when(userService.getAuthenticatedUser()).thenReturn(user);

        assertThrows(ResponseStatusException.class, () -> messageService.markRead(message.getId()));
    }

    @Test
    public void markRead_WhenUserNotAuthorized() {
        when(messageRepository.findById(message.getId())).thenReturn(Optional.of(message));
        when(userService.getAuthenticatedUser()).thenReturn(anotherUser);

        assertThrows(ResponseStatusException.class,
                () -> messageService.markRead(message.getId()));
    }

    @Test
    public void search_Success() {
        project.getMessages().add(message);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(userService.getAuthenticatedUser()).thenReturn(user);

        List<MessageReturnDTO> result = messageService.search(project.getId(), "Mensagem");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(message.getText(), result.get(0).getText());
    }

    @Test
    public void search_WhenProjectNotFound() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.empty());
        when(userService.getAuthenticatedUser()).thenReturn(user);

        assertThrows(ResponseStatusException.class,
                () -> messageService.search(project.getId(), "Mensagem"));
    }

    @Test
    public void search_WhenMessageNotFound() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(userService.getAuthenticatedUser()).thenReturn(user);

        List<MessageReturnDTO> result = messageService.search(project.getId(), "none");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void getMessageById_Success() {
        when(messageRepository.findById(message.getId())).thenReturn(Optional.of(message));
        when(userService.getAuthenticatedUser()).thenReturn(user);

        MessageReturnDTO result = messageService.getMessageById(message.getId());

        assertEquals(message.getText(), result.getText());
    }

    @Test
    public void getMessageById_WhenMessageNotFound() {
        when(messageRepository.findById(message.getId())).thenReturn(Optional.empty());
        when(userService.getAuthenticatedUser()).thenReturn(user);

        assertThrows(ResponseStatusException.class, () -> messageService.getMessageById(message.getId()));
    }

    @Test
    public void deleteMessage_Success_AsAuthor() {
        when(messageRepository.findById(message.getId())).thenReturn(Optional.of(message));
        when(userService.getAuthenticatedUser()).thenReturn(user);

        messageService.deleteMessage(message.getId());

        verify(messageRepository).delete(any(Message.class));
    }

    @Test
    public void deleteMessage_Success_AsAdmin() {
        user.setRole(RoleName.ROLE_ADMIN);
        project.getUsers().add(user);

        when(messageRepository.findById(message.getId())).thenReturn(Optional.of(message));
        when(userService.getAuthenticatedUser()).thenReturn(user);

        messageService.deleteMessage(message.getId());

        verify(messageRepository).delete(any(Message.class));
    }

    @Test
    public void deleteMessage_WhenUserNotAuthorized() {
        when(messageRepository.findById(message.getId())).thenReturn(Optional.of(message));
        when(userService.getAuthenticatedUser()).thenReturn(anotherUser);

        assertThrows(ResponseStatusException.class, () -> messageService.deleteMessage(message.getId()));
    }

    @Test
    public void deleteMessage_WhenMessageNotFound() {
        when(messageRepository.findById(message.getId())).thenReturn(Optional.empty());
        when(userService.getAuthenticatedUser()).thenReturn(user);

        assertThrows(ResponseStatusException.class, () -> messageService.deleteMessage(message.getId()));
    }
}