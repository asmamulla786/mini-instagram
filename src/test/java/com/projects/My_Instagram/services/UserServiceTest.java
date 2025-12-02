package com.projects.My_Instagram.services;

import com.projects.My_Instagram.DTOs.request.UserRequest;
import com.projects.My_Instagram.DTOs.response.UserResponse;
import com.projects.My_Instagram.exceptions.UserNameExistsException;
import com.projects.My_Instagram.exceptions.UserNameNullException;
import com.projects.My_Instagram.exceptions.UserNotFoundException;
import com.projects.My_Instagram.models.Role;
import com.projects.My_Instagram.models.User;
import com.projects.My_Instagram.repositories.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static com.projects.My_Instagram.constants.exception.ExceptionMessages.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void createUserSuccessfully() {
        UserRequest userRequest = new UserRequest();
        userRequest.setFullName("Asma Mulla");
        userRequest.setUsername("asma_123");
        userRequest.setPassword("password123");
        userRequest.setProfilePicUrl("http://example.com/image1.jpg");
        userRequest.setPrivateAccount(false);

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("asma_123");
        savedUser.setFullName("Asma Mulla");
        savedUser.setProfilePicUrl("http://example.com/image1.jpg");
        savedUser.setRole(Role.USER);
        savedUser.setPrivateAccount(false);

        Mockito.when(userRepository.existsByUsername("asma_123")).thenReturn(false);
        Mockito.when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(savedUser);

        UserResponse userResponse = userService.createUser(userRequest);

        Assertions.assertEquals(userRequest.getUsername(), userResponse.getUsername());
        Assertions.assertEquals(userRequest.getFullName(), userResponse.getFullName());
        Assertions.assertEquals(userRequest.getProfilePicUrl(), userResponse.getProfilePicUrl());

        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
    }

    @Test
    void usernameExistsException() {
        UserRequest userRequest = new UserRequest();
        userRequest.setFullName("Asma Mulla");
        userRequest.setUsername("asma_123");
        userRequest.setProfilePicUrl("http://example.com/image1.jpg");

        Mockito.when(userRepository.existsByUsername("asma_123")).thenReturn(true);

        UserNameExistsException userNameExistsException = assertThrows(UserNameExistsException.class, () -> userService.createUser(userRequest));

        Assertions.assertEquals(USER_NAME_EXISTS.getMessage(), userNameExistsException.getMessage());
        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any(User.class));
    }

    @Test
    void usernameNull(){
        UserRequest userRequest = new UserRequest();

        UserNameNullException userNameNullException = assertThrows(UserNameNullException.class, () -> userService.createUser(userRequest));

        Assertions.assertEquals(USER_NAME_NULL.getMessage(), userNameNullException.getMessage());
        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any(User.class));
    }

    @Test
    void getUserById() {
        User user = new User();
        user.setId(1L);
        user.setFullName("AsmaMulla");
        user.setUsername("ashu@123");
        user.setProfilePicUrl("http://example.com/image1.jpg");
        user.setPrivateAccount(false);

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse fetchedUser = userService.getUserById(1L);

        Assertions.assertEquals(user.getId(), fetchedUser.getId());
        Assertions.assertEquals(user.getFullName(), fetchedUser.getFullName());
        Assertions.assertEquals(user.getUsername(), fetchedUser.getUsername());
        Assertions.assertEquals(user.getProfilePicUrl(), fetchedUser.getProfilePicUrl());

        Mockito.verify(userRepository, Mockito.times(1)).findById(1L);
    }

    @Test
    void userNotFound(){
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.empty());

        UserNotFoundException userNotFoundException = assertThrows(UserNotFoundException.class, () -> userService.getUserById(1L));
        Assertions.assertEquals(USER_NOT_FOUND.getMessage(),userNotFoundException.getMessage());
        Mockito.verify(userRepository, Mockito.times(1)).findById(1L);
    }

    @Test
    void getAllUsers() {
        User user = new User();
        user.setFullName("AsmaMulla");
        user.setUsername("ashu@123");
        user.setProfilePicUrl("http://example.com/image1.jpg");
        user.setPrivateAccount(false);

        List<User> users = List.of(user);

        Mockito.when(userRepository.findAll()).thenReturn(users);

        List<UserResponse> allUsers = userService.getAllUsers();

        Assertions.assertEquals(1, allUsers.size());
    }

    @Test
    void updateUserWhenUserExists() {
        Long userId = 1L;
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setUsername("asma_123");
        existingUser.setFullName("Asma Mulla");
        existingUser.setPrivateAccount(false);

        UserRequest updatedUser = new UserRequest();
        updatedUser.setUsername("asma_456");
        updatedUser.setFullName("Asma Khan");

        User savedUser = new User();
        savedUser.setId(userId);
        savedUser.setUsername("asma_456");
        savedUser.setFullName("Asma Khan");
        savedUser.setPrivateAccount(false);

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        Mockito.when(userRepository.existsByUsername("asma_456")).thenReturn(false);
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(savedUser);

        UserResponse userResponse = userService.updateUser(userId, updatedUser);

        Assertions.assertEquals("asma_456", userResponse.getUsername());
        Assertions.assertEquals("Asma Khan", userResponse.getFullName());

        Mockito.verify(userRepository, Mockito.times(1)).findById(userId);
        Mockito.verify(userRepository, Mockito.times(1)).save(existingUser);
    }

    @Test
    void deleteUser() {
        Long userId = 1L;
        User existingUser = new User();
        existingUser.setId(userId);

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        userService.deleteUser(userId);

        Mockito.verify(userRepository, Mockito.times(1)).findById(userId);
        Mockito.verify(userRepository, Mockito.times(1)).delete(existingUser);
    }
}