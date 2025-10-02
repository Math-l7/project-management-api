package project_management_api.project_management_api.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.transaction.Transactional;
import project_management_api.project_management_api.dto.NotificationInputDTOToProject;
import project_management_api.project_management_api.dto.UserInputDTO;
import project_management_api.project_management_api.dto.UserLoginReturnDTO;
import project_management_api.project_management_api.dto.UserReturnDTO;
import project_management_api.project_management_api.dto.UserUpdateDTO;
import project_management_api.project_management_api.enums.RoleName;
import project_management_api.project_management_api.model.Project;
import project_management_api.project_management_api.model.User;
import project_management_api.project_management_api.repository.UserRepository;
import org.springframework.context.annotation.Lazy;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder bcrypt;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwt;
    private final NotificationService notificationService;

    public UserService(UserRepository userRepository, PasswordEncoder bcrypt,
            AuthenticationManager authenticationManager, JwtService jwt,
            @Lazy NotificationService notificationService) {
        this.userRepository = userRepository;
        this.bcrypt = bcrypt;
        this.authenticationManager = authenticationManager;
        this.jwt = jwt;
        this.notificationService = notificationService;
    }

    public UserReturnDTO toUserDTO(User user) {
        return new UserReturnDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole() != null ? user.getRole().name() : "ROLE_USER");
    }

    public User findUserByIdOrThrow(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
    }

    public User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Usuário não autenticado no contexto."));
    }
    //

    @Transactional
    public UserReturnDTO registerUser(UserInputDTO user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário já cadastrado com este email.");
        }
        if (!user.getPassword().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Sua senha precisa ter caracteres especiais.");
        }

        String passwordBcrypt = bcrypt.encode(user.getPassword());
        user.setPassword(passwordBcrypt);

        User userRegister = new User();
        userRegister.setEmail(user.getEmail());
        userRegister.setName(user.getName());
        userRegister.setPassword(passwordBcrypt);

        userRepository.save(userRegister);
        return toUserDTO(userRegister);

    }

    public UserLoginReturnDTO login(UserInputDTO user) {
        authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));

        User userLogin = userRepository.findByEmail(user.getEmail()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhum usuário identificado com esse email."));

        String token = jwt.generateToken(user.getEmail());

        return new UserLoginReturnDTO(token, userLogin.getId(), userLogin.getName(), userLogin.getEmail(),
                userLogin.getRole().toString());

    }

    public UserReturnDTO getUserById(Integer id) {
        User user = findUserByIdOrThrow(id);

        return toUserDTO(user);
    }

    @Transactional
    public UserReturnDTO updateUser(UserUpdateDTO userDTO) {

        User user = getAuthenticatedUser();

        if (!bcrypt.matches(userDTO.getCurrentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Senha incorreta. Sua senha não deve ser alterada neste campo.");
        }

        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail());
        }
        if (userDTO.getName() != null) {
            user.setName(userDTO.getName());
        }

        userRepository.save(user);

        return toUserDTO(user);
    }

    @Transactional
    public UserReturnDTO changePassword(String oldPassword, String newPassword) {

        User user = getAuthenticatedUser();

        if (!bcrypt.matches(oldPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha incorreta.");
        }

        if (newPassword == null || bcrypt.matches(newPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Você deve definir uma nova senha.");
        }

        user.setPassword(bcrypt.encode(newPassword));
        userRepository.save(user);
        return toUserDTO(user);

    }

    @Transactional
    public UserReturnDTO updateUserRole(Integer idUser, RoleName role) {
        User user = findUserByIdOrThrow(idUser);

        if (role.equals(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role já atribuída a este usuário.");
        }
        user.setRole(role);
        userRepository.save(user);

        List<Project> projects = user.getProjects();

        for (Project project : projects) {
            notificationService.sendNotificationToProject(new NotificationInputDTOToProject(
                    user.getName() + " agora é " + role.toString(), project.getId()));
        }

        return toUserDTO(user);
    }

    public List<UserReturnDTO> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(user -> toUserDTO(user))
                .toList();
    }

    public List<UserReturnDTO> getUsersByRole(String role) {
        RoleName roleEnum = RoleName.valueOf(role);
        List<User> users = userRepository.findByRole(roleEnum);

        List<UserReturnDTO> listDTO = new ArrayList<>();
        users.forEach(user -> listDTO.add(toUserDTO(user)));

        return listDTO;
    }

    @Transactional
    public void deleteMe() {

        User userLogged = getAuthenticatedUser();

        userRepository.delete(userLogged);
    }

    @Transactional
    public void deleteUser(Integer userToDeleteId) {
        User user = findUserByIdOrThrow(userToDeleteId);

        userRepository.delete(user);
    }

}
