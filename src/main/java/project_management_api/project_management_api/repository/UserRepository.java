package project_management_api.project_management_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import project_management_api.project_management_api.enums.RoleName;
import project_management_api.project_management_api.model.User;

public interface UserRepository extends JpaRepository<User, Integer> {

    public Optional<User> findByEmail(String email);

    List<User> findByRole(RoleName roleName);

    public boolean existsByEmail(String email);

}
