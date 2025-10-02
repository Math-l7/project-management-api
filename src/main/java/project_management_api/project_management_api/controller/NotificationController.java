package project_management_api.project_management_api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import project_management_api.project_management_api.dto.NotificationReturnDTO;
import project_management_api.project_management_api.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/me")
    public ResponseEntity<List<NotificationReturnDTO>> getNotificationsByUser() {
        return ResponseEntity.ok(notificationService.getNotificationsByUser());
    }

    @GetMapping("/me/not-read")
    public ResponseEntity<List<NotificationReturnDTO>> getNotificationsNotRead() {
        return ResponseEntity.ok(notificationService.getNotificationsNotRead());
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<NotificationReturnDTO> markRead(@PathVariable Integer notificationId) {
        return ResponseEntity.ok(notificationService.markRead(notificationId));
    }

    @PutMapping("/me/read-all")
    public ResponseEntity<List<NotificationReturnDTO>> markAllRead() {
        return ResponseEntity.ok(notificationService.markAllRead());
    }

    @DeleteMapping("/{notificationId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteNotification(@PathVariable Integer notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.noContent().build();
    }

}
