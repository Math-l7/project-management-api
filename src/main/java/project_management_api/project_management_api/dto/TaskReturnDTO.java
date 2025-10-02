package project_management_api.project_management_api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskReturnDTO {
    private Integer id;
    private String title;
    private String description;
    private String status;
    private Integer projectId;
    private Integer taskOwnerId;
}
