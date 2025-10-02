package project_management_api.project_management_api.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project_management_api.project_management_api.enums.MessageStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageReturnDTO {
    private Integer id;
    private String text;
    private LocalDateTime time;
    private MessageStatus status;
    private Integer projectId;
    private Integer userId;
}
