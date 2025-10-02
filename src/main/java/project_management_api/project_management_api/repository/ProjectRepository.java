package project_management_api.project_management_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import project_management_api.project_management_api.model.Project;

public interface ProjectRepository extends JpaRepository<Project, Integer> {

    public Project findByName(String name);

    public boolean existsByName(String name);

    List<Project> findByUsers_Id(Integer userId);

}
