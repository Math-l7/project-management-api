package project_management_api.project_management_api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project_management_api.project_management_api.enums.MessageStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageUpdateDTO {
     private MessageStatus status;
}
