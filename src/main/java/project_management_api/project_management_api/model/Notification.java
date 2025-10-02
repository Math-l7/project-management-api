package project_management_api.project_management_api.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import project_management_api.project_management_api.enums.NotificationStatus;

@Entity
@Getter
@Setter
public class Notification {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Integer id;

    @Column(nullable = false)
    private String textNotification;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status = NotificationStatus.NOT_READ;

    @Column(nullable = false)
    private LocalDateTime time = LocalDateTime.now();
    @ManyToOne
    @JoinColumn(nullable = false)
    private User userDestin;
}
