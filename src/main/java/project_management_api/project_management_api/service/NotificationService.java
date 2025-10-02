package project_management_api.project_management_api.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.transaction.Transactional;

import project_management_api.project_management_api.controller.SseController;
import project_management_api.project_management_api.dto.NotificationInputDTOToProject;
import project_management_api.project_management_api.dto.NotificationInputDTOToUser;
import project_management_api.project_management_api.dto.NotificationReturnDTO;
import project_management_api.project_management_api.dto.NotificationReturnDTOTProject;
import project_management_api.project_management_api.enums.NotificationStatus;
import project_management_api.project_management_api.model.Notification;
import project_management_api.project_management_api.model.Project;
import project_management_api.project_management_api.model.User;
import project_management_api.project_management_api.repository.NotificationRepository;
import project_management_api.project_management_api.repository.ProjectRepository;
import project_management_api.project_management_api.repository.UserRepository;
import org.springframework.context.annotation.Lazy;

@Service
public class NotificationService {

        private final NotificationRepository notificationRepository;
        private final UserRepository userRepository;
        private final ProjectRepository projectRepository;
        private final SseController sseController; // Mantido, pois é o responsável pela tecnologia SSE
        private final UserService userService;

        public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository,
                        ProjectRepository projectRepository, SseController sseController,
                        @Lazy UserService userService) {
                this.notificationRepository = notificationRepository;
                this.userRepository = userRepository;
                this.projectRepository = projectRepository;
                this.sseController = sseController;
                this.userService = userService;
        }

        public NotificationReturnDTO toNotificationDTO(Notification notification) {
                return new NotificationReturnDTO(
                                notification.getId(),
                                notification.getTextNotification(),
                                notification.getStatus(),
                                notification.getTime(),
                                notification.getUserDestin() != null ? notification.getUserDestin().getId() : null);
        }

        private User findUserOrThrow(Integer userId) {
                return userRepository.findById(userId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Usuário não encontrado: " + userId));
        }

        private Project findProjectOrThrow(Integer projectId) {
                return projectRepository.findById(projectId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Projeto não encontrado: " + projectId));
        }

        private Notification findNotificationOrThrow(Integer notificationId) {
                return notificationRepository.findById(notificationId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Notificação não encontrada: " + notificationId));
        }

        @Transactional
        public NotificationReturnDTOTProject sendNotificationToProject(NotificationInputDTOToProject notificationDto) {
                Project project = findProjectOrThrow(notificationDto.getProjectId());

                List<Notification> notifications = project.getUsers().stream()
                                .map(user -> {
                                        Notification n = new Notification();
                                        n.setTextNotification(notificationDto.getTextNotification());
                                        n.setTime(LocalDateTime.now());
                                        n.setUserDestin(user);
                                        return n;
                                })
                                .toList();

                notificationRepository.saveAll(notifications);

                sseController.sendNotification(project.getName() + "| " + project.getStatus() + "\n"
                                + notificationDto.getTextNotification());
                return new NotificationReturnDTOTProject(project.getId(), notificationDto.getTextNotification(),
                                notifications.size());

        }

        @Transactional
        public NotificationReturnDTO sendNotificationToUser(NotificationInputDTOToUser notificationDto) {
                User user = findUserOrThrow(notificationDto.getUserDestinId());

                Notification notification = new Notification();
                notification.setTextNotification(notificationDto.getTextNotification());
                notification.setTime(LocalDateTime.now());
                notification.setUserDestin(user);

                notificationRepository.save(notification);
                sseController.sendNotification(user.getName() + ":\n" + notification.getTextNotification());
                return toNotificationDTO(notification);
        }

        @Transactional
        public NotificationReturnDTO markRead(Integer notificationId) {

                User user = userService.getAuthenticatedUser();
                Notification notification = findNotificationOrThrow(notificationId);

                if (!notification.getUserDestin().getId().equals(user.getId())) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                        "Você não tem permissão para ler essa notificação.");
                }

                if (notification.getStatus().equals(NotificationStatus.READ)) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Essa mensagem já havia sido lida.");
                }

                notification.setStatus(NotificationStatus.READ);
                notificationRepository.save(notification);
                return toNotificationDTO(notification);
        }

        @Transactional
        public List<NotificationReturnDTO> markAllRead() {
                User user = userService.getAuthenticatedUser();

                List<Notification> list = user.getNotifications().stream()
                                .map(n -> {
                                        n.setStatus(NotificationStatus.READ);
                                        return n;
                                }).toList();

                notificationRepository.saveAll(list);

                return list.stream().map(n -> toNotificationDTO(n)).toList();

        }

        public List<NotificationReturnDTO> getNotificationsByUser() {
                User user = userService.getAuthenticatedUser();

                return user.getNotifications().stream().map(n -> toNotificationDTO(n)).toList();

        }

        public List<NotificationReturnDTO> getNotificationsNotRead() {
                User user = userService.getAuthenticatedUser();

                return user.getNotifications().stream().filter(n -> n.getStatus().equals(NotificationStatus.NOT_READ))
                                .map(n -> toNotificationDTO(n)).toList();

        }

        @Transactional
        public void deleteNotification(Integer notificationId) {
                Notification notification = findNotificationOrThrow(notificationId);

                notificationRepository.delete(notification);

        }

}
