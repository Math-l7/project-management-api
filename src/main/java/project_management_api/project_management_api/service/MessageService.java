package project_management_api.project_management_api.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.transaction.Transactional;
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
import org.springframework.context.annotation.Lazy;

@Service
public class MessageService {

        private final ProjectRepository projectRepository;
        private final MessageRepository messageRepository;
        private final NotificationService notificationService;
        private final UserService userService;

        public MessageService(ProjectRepository projectRepository,
                        MessageRepository messageRepository, @Lazy NotificationService notificationService,
                        UserService userService) {

                this.projectRepository = projectRepository;
                this.messageRepository = messageRepository;
                this.notificationService = notificationService;
                this.userService = userService;
        }

        private Project findProjectById(Integer projectId) {
                return projectRepository.findById(projectId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Projeto não encontrado."));
        }

        private Message findMessageById(Integer messageId) {
                return messageRepository.findById(messageId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Mensagem não encontrada."));
        }

        public MessageReturnDTO toMessageDTO(Message message) {
                return new MessageReturnDTO(
                                message.getId(),
                                message.getText(),
                                message.getTime(),
                                message.getStatus(),
                                message.getProject() != null ? message.getProject().getId() : null,
                                message.getUser() != null ? message.getUser().getId() : null);
        }

        @Transactional
        public MessageReturnDTO sendMessage(Integer projectId, MessageInputDTO messageDto) {

                User sender = userService.getAuthenticatedUser();

                Project project = findProjectById(projectId);

                if (!project.getUsers().contains(sender)) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                        "Você não pode enviar mensagens para este projeto.");
                }

                Message message = new Message();
                message.setProject(project);
                message.setUser(sender);
                message.setText(messageDto.getText());
                message.setTime(LocalDateTime.now());

                messageRepository.save(message);
                notificationService.sendNotificationToProject(new NotificationInputDTOToProject(
                                sender.getName() + ": \n" + messageDto.getText(), projectId));
                return toMessageDTO(message);
        }

        @Transactional
        public MessageReturnDTO markRead(Integer messageId) {

                Message message = findMessageById(messageId);

                User user = userService.getAuthenticatedUser();

                if (message.getStatus().equals(MessageStatus.READ)) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mensagem já marcada como lida.");
                }

                if (!message.getProject().getUsers().contains(user)) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário não autorizado.");
                }

                message.setStatus(MessageStatus.READ);
                messageRepository.save(message);
                return toMessageDTO(message);

        }

        public List<MessageReturnDTO> search(Integer projectId, String text) {
                Project project = findProjectById(projectId);

                User user = userService.getAuthenticatedUser();

                if (!project.getUsers().contains(user)) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                        "Usuário não tem permissão para acessar este projeto.");
                }

                List<MessageReturnDTO> list = project.getMessages().stream()
                                .filter(m -> m.getText().toLowerCase().contains(text.toLowerCase()))
                                .map(m -> toMessageDTO(m)).toList();

                return list;
        }

        public MessageReturnDTO getMessageById(Integer messageId) {

                Message message = findMessageById(messageId);

                User user = userService.getAuthenticatedUser();

                if (!message.getProject().getUsers().contains(user)) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                        "Usuário não tem permissão para acessar esta mensagem.");
                }

                return toMessageDTO(message);
        }

        @Transactional
        public void deleteMessage(Integer messageId) {

                Message message = findMessageById(messageId);
                User user = userService.getAuthenticatedUser();
                Project project = message.getProject();

                boolean isAuthor = message.getUser().equals(user);
                boolean isProjectAdmin = project.getUsers().stream()
                                .anyMatch(u -> u.getId().equals(user.getId()) && u.getRole() == RoleName.ROLE_ADMIN);

                if (!isAuthor && !isProjectAdmin) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                        "Você não tem permissão para apagar mensagens de outros usuários.");
                }

                notificationService.sendNotificationToProject(
                                new NotificationInputDTOToProject(
                                                user.getName() + " apagou uma mensagem no projeto "
                                                                + project.getName() + ".",
                                                project.getId()));
                messageRepository.delete(message);

        }

}
