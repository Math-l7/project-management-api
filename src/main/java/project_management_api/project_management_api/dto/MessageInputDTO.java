package project_management_api.project_management_api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageInputDTO {

    private String text;
    private Integer projectId;
    private Integer userId;

}
