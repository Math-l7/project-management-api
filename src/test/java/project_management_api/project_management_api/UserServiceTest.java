package project_management_api.project_management_api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import project_management_api.project_management_api.dto.UserInputDTO;
import project_management_api.project_management_api.dto.UserLoginReturnDTO;
import project_management_api.project_management_api.dto.UserReturnDTO;
import project_management_api.project_management_api.dto.UserUpdateDTO;
import project_management_api.project_management_api.enums.RoleName;
import project_management_api.project_management_api.model.User;
import project_management_api.project_management_api.repository.UserRepository;
import project_management_api.project_management_api.service.JwtService;
import project_management_api.project_management_api.service.NotificationService;
import project_management_api.project_management_api.service.UserService;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    private User user;
    private UserInputDTO dto;
    private UserUpdateDTO userUpdate;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder bcrypt;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private NotificationService notificationService;

    @Mock
    private JwtService jwt;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void before() {
        user = new User();
        user.setId(1);
        user.setName("User");
        user.setEmail("UserEmail");
        user.setPassword("password@");
        user.setRole(RoleName.ROLE_USER);
        user.setProjects(new ArrayList<>());

        dto = new UserInputDTO();
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setPassword("password@");

        userUpdate = new UserUpdateDTO();
        userUpdate.setEmail("newEmail");
        userUpdate.setName("newName");
        userUpdate.setCurrentPassword("password@");
    }

    private void mockAuthenticatedUser() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user.getEmail(), null,
                new ArrayList<>());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    @Test
    public void registerUser_Success() {
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(bcrypt.encode(dto.getPassword())).thenReturn("hash");

        UserReturnDTO result = userService.registerUser(dto);

        assertNotNull(result);
        assertEquals("User", result.getName());
        assertEquals("UserEmail", result.getEmail());

        verify(userRepository).save(any(User.class));
        verify(bcrypt).encode(dto.getPassword());
    }

    @Test
    public void registerUser_WhenPasswordDontHaveCharacters() {
        dto.setPassword("password");
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> userService.registerUser(dto));
    }

    @Test
    public void registerUser_WhenEmailAlreadyExists() {
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> userService.registerUser(dto));
    }

    @Test
    public void login_Success() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(jwt.generateToken(user.getEmail())).thenReturn("token");

        UserLoginReturnDTO result = userService.login(dto);

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getName(), result.getName());
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getRole().toString(), result.getRole());

        verify(userRepository).findByEmail(dto.getEmail());
        verify(jwt).generateToken(dto.getEmail());
    }

    @Test
    public void login_WhenEmailNotFound() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> userService.login(dto));
    }

    @Test
    public void updateUser_Success() {
        mockAuthenticatedUser();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(bcrypt.matches(userUpdate.getCurrentPassword(), user.getPassword())).thenReturn(true);

        UserReturnDTO result = userService.updateUser(userUpdate);

        assertEquals("newEmail", result.getEmail());
        assertEquals("newName", result.getName());
    }

    @Test
    public void updateUser_WhenWrongPassword() {
        mockAuthenticatedUser();

        userUpdate.setCurrentPassword("wrongPassword");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(bcrypt.matches(userUpdate.getCurrentPassword(), user.getPassword())).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> userService.updateUser(userUpdate));
    }

    @Test
    public void changePassword_Success() {
        mockAuthenticatedUser();

        String oldPassword = user.getPassword();
        String newPassword = "newPassword@";

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(bcrypt.matches(oldPassword, user.getPassword())).thenReturn(true);
        when(bcrypt.matches(newPassword, user.getPassword())).thenReturn(false);
        when(bcrypt.encode(newPassword)).thenReturn("encodedNew");

        UserReturnDTO result = userService.changePassword(user.getPassword(), newPassword);

        assertNotNull(result);
        verify(bcrypt).encode(newPassword);
        verify(userRepository).save(user);
    }

    @Test
    public void changePassword_WhenWrongPassword() {
        mockAuthenticatedUser();

        String oldPassword = "wrongPassword@";
        String newPassword = "newPassword@";

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(bcrypt.matches(oldPassword, user.getPassword())).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> userService.changePassword(oldPassword, newPassword));
    }

    @Test
    public void changePassword_WhenNewIsTheSame() {
        mockAuthenticatedUser();

        String oldPassword = user.getPassword();
        String newPassword = oldPassword;

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(bcrypt.matches(oldPassword, user.getPassword())).thenReturn(true);
        when(bcrypt.matches(newPassword, user.getPassword())).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> userService.changePassword(oldPassword, newPassword));
    }

    @Test
    public void updateUserRole_Success() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        doNothing().when(notificationService).sendNotificationToProject(any());

        UserReturnDTO result = userService.updateUserRole(user.getId(), RoleName.ROLE_ADMIN);

        assertEquals(RoleName.ROLE_ADMIN.toString(), result.getRole());

        verify(userRepository).save(user);
        verify(notificationService).sendNotificationToProject(any());
    }

    @Test
    public void updateUserRole_WhenRoleIsTheSame() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThrows(ResponseStatusException.class, () -> userService.updateUserRole(user.getId(), RoleName.ROLE_USER));
    }

    @Test
    public void updateUserRole_WhenUserNotFound() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> userService.updateUserRole(user.getId(), RoleName.ROLE_ADMIN));
    }

    @Test
    public void deleteMe_Success() {
        mockAuthenticatedUser();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);

        userService.deleteMe();

        verify(userRepository).delete(user);
    }

    @Test
    public void deleteUser_Success() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);

        userService.deleteUser(user.getId());

        verify(userRepository).delete(user);
    }

}