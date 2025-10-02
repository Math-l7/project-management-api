package project_management_api.project_management_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import project_management_api.project_management_api.enums.TaskStatus;
import project_management_api.project_management_api.model.Task;

public interface TaskRepository extends JpaRepository<Task, Integer> {

    List<Task> findByProjectOwner_Id(Integer projectId);

    List<Task> findByStatus(TaskStatus status);

    public boolean existsByTitle(String title);

    List<Task> findByProjectOwnerId(Integer projectId);

    List<Task> findByTaskOwnerId(Integer userId);

    boolean existsByTitleAndProjectOwnerId(String title, Integer projectId);

}
