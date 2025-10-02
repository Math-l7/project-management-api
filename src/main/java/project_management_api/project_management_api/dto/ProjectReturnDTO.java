package project_management_api.project_management_api.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project_management_api.project_management_api.enums.ProjectStatus;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProjectReturnDTO {
    private Integer id;
    private String name;
    private String description;
    private List<UserReturnDTO> members;
    private ProjectStatus status;
}
