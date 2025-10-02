package project_management_api.project_management_api.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.transaction.Transactional;
import project_management_api.project_management_api.dto.NotificationInputDTOToProject;
import project_management_api.project_management_api.dto.NotificationInputDTOToUser;
import project_management_api.project_management_api.dto.ProjectInputDTO;
import project_management_api.project_management_api.dto.ProjectReturnDTO;
import project_management_api.project_management_api.dto.ProjectUpdateDTO;
import project_management_api.project_management_api.enums.ProjectStatus;
import project_management_api.project_management_api.model.Project;
import project_management_api.project_management_api.model.User;
import project_management_api.project_management_api.repository.ProjectRepository;
import project_management_api.project_management_api.repository.UserRepository;

@Service
public class ProjectService {

        private final ProjectRepository projectRepository;
        private final UserRepository userRepository;
        private final UserService userService;
        private final NotificationService notificationService;

        public ProjectService(ProjectRepository projectRepository, UserRepository userRepository,
                        UserService userService, NotificationService notificationService) {
                this.projectRepository = projectRepository;
                this.userRepository = userRepository;
                this.userService = userService;
                this.notificationService = notificationService;
        }

        public ProjectReturnDTO toProjectDTO(Project project) {
                return new ProjectReturnDTO(
                                project.getId(),
                                project.getName(),
                                project.getDescription(),
                                project.getUsers()
                                                .stream()
                                                .map(userService::toUserDTO)
                                                .toList(),
                                project.getStatus());
        }

        private Project findProjectById(Integer projectId) {
                return projectRepository.findById(projectId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Projeto não encontrado."));
        }

        private User findUserById(Integer userId) {
                return userRepository.findById(userId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Usuário não encontrado."));
        }

        @Transactional
        public ProjectReturnDTO createProject(ProjectInputDTO projectDto) {

                if (projectRepository.existsByName(projectDto.getName())) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Projeto já cadastrado.");
                }

                Project project = new Project();
                project.setName(projectDto.getName());
                project.setDescription(projectDto.getDescription());

                User creator = userService.getAuthenticatedUser();

                project.setUsers(List.of(creator));

                Project projectToSave = projectRepository.save(project);

                notificationService.sendNotificationToUser(
                                new NotificationInputDTOToUser("Projeto " + project.getName() + " criado com sucesso!",
                                                creator.getId()));
                return toProjectDTO(projectToSave);

        }

        @Transactional
        public ProjectReturnDTO updateProject(ProjectUpdateDTO projectDTO, Integer projectId) {

                Project project = findProjectById(projectId);

                User userUpdater = userService.getAuthenticatedUser();

                if (projectDTO.getName() != null && !project.getName().equals(projectDTO.getName())) {
                        project.setName(projectDTO.getName());
                }

                if (projectDTO.getDescription() != null
                                && !project.getDescription().equals(projectDTO.getDescription())) {
                        project.setDescription(projectDTO.getDescription());
                }

                Project projectToSave = projectRepository.save(project);
                notificationService.sendNotificationToProject(new NotificationInputDTOToProject(
                                userUpdater.getName() + " atualizou dados do projeto " + project.getName() + ".",
                                projectId));
                return toProjectDTO(projectToSave);

        }

        @Transactional
        public ProjectReturnDTO updateStatusProject(Integer projectId, ProjectStatus status) {

                Project project = findProjectById(projectId);

                User userUpdater = userService.getAuthenticatedUser();

                if (project.getStatus().equals(status)) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status atribuído anteriormente.");
                }

                project.setStatus(status);
                Project projectToSave = projectRepository.save(project);
                notificationService.sendNotificationToProject(new NotificationInputDTOToProject(
                                userUpdater.getName() + " atualizou o status do projeto " + project.getName() + ".",
                                projectId));
                return toProjectDTO(projectToSave);

        }

        public List<ProjectReturnDTO> getProjectsByUser(Integer userId) {
                User userLogged = userService.getAuthenticatedUser();

                boolean isAdmin = userLogged.getRole().name().equals("ROLE_ADMIN");

                if (!isAdmin && userId != null) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado.");
                }

                List<Project> projects;
                if (isAdmin) {
                        if (userId == null) {
                                projects = projectRepository.findAll();
                        } else {
                                projects = projectRepository.findByUsers_Id(userId);
                        }
                } else {
                        projects = projectRepository.findByUsers_Id(userLogged.getId());
                }

                return projects.stream().map(this::toProjectDTO).toList();
        }

        public ProjectReturnDTO getProjectById(Integer idProject) {
                Project project = findProjectById(idProject);
                User userLogged = userService.getAuthenticatedUser();

                boolean isAdmin = userLogged.getRole().name().equals("ROLE_ADMIN");

                if (!isAdmin && !project.getUsers().contains(userLogged)) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado.");
                }

                return toProjectDTO(project);
        }

        @Transactional
        public ProjectReturnDTO addUserToProject(Integer idProject, Integer idUser) {

                Project project = findProjectById(idProject);
                User user = findUserById(idUser);

                if (project.getUsers().contains(user)) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                        "Este usuário já faz parte do projeto.");
                }

                project.getUsers().add(user);

                Project projectToReturn = projectRepository.save(project);
                notificationService.sendNotificationToProject(new NotificationInputDTOToProject(
                                user.getName() + " foi adicionado ao projeto " + project.getName() + ".", idProject));
                return toProjectDTO(projectToReturn);

        }

        @Transactional
        public ProjectReturnDTO removeUserFromProject(Integer idProject, Integer idUser) {
                Project project = findProjectById(idProject);

                User user = findUserById(idUser);

                if (!project.getUsers().contains(user)) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                        "Este usuário não faz parte do projeto.");
                }

                project.getUsers().remove(user);

                Project projectToReturn = projectRepository.save(project);
                notificationService.sendNotificationToProject(new NotificationInputDTOToProject(
                                user.getName() + " foi removido do projeto " + project.getName() + ".", idProject));
                return toProjectDTO(projectToReturn);
        }

        @Transactional
        public void deleteProject(Integer projectId) {
                Project project = findProjectById(projectId);

                List<User> users = project.getUsers();
                String nameProject = project.getName();

                for (User user : users) {
                        notificationService.sendNotificationToUser(new NotificationInputDTOToUser(
                                        "O projeto " + nameProject + " foi deletado.", user.getId()));

                }
                projectRepository.delete(project);
        }

}
