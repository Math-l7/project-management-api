package project_management_api.project_management_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import project_management_api.project_management_api.model.Message;

public interface MessageRepository extends JpaRepository<Message, Integer> {

    List<Message> findByProject_Id(Integer projectId);

    List<Message> findByUser_Id(Integer userId);

}
