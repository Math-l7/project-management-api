package project_management_api.project_management_api.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project_management_api.project_management_api.enums.NotificationStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationReturnDTO {
    private Integer id;
    private String textNotification;
    private NotificationStatus status;
    private LocalDateTime time;
    private Integer userDestinId;
}
