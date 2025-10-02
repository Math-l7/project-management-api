package project_management_api.project_management_api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationReturnDTOTProject {
    private Integer projectId;
    private String textNotification;
    private int totalUsersNotified;
}
