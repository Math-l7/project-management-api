package project_management_api.project_management_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import project_management_api.project_management_api.enums.NotificationStatus;
import project_management_api.project_management_api.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findByUserDestin_Id(Integer userId);

    List<Notification> findByUserDestin_IdAndStatus(Integer userId, NotificationStatus status);

}
