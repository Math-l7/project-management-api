package project_management_api.project_management_api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserLoginReturnDTO {

    private String token;
    private Integer id;
    private String name;
    private String email;
    private String role;
}
