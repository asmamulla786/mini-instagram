package com.projects.My_Instagram.services;

import com.projects.My_Instagram.DTOs.request.UserRequest;
import com.projects.My_Instagram.exceptions.UserNameExistsException;
import com.projects.My_Instagram.exceptions.UserNameNullException;
import com.projects.My_Instagram.exceptions.UserNotFoundException;
import com.projects.My_Instagram.models.User;
import com.projects.My_Instagram.repositories.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.projects.My_Instagram.constants.exception.ExceptionMessages.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void createUserSuccessfully() {
        UserRequest userRequest = new UserRequest();
        userRequest.setFullName("Asma Mulla");
        userRequest.setUsername("asma_123");
        userRequest.setProfilePicUrl("http://example.com/image1.jpg");

        Mockito.when(userRepository.existsByUsername("asma_123")).thenReturn(false);

        Mockito.when(userRepository.save(Mockito.any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        User user = userService.createUser(userRequest);

        Assertions.assertEquals(userRequest.getUsername(), user.getUsername());
        Assertions.assertEquals(userRequest.getFullName(), user.getFullName());
        Assertions.assertEquals(userRequest.getProfilePicUrl(), user.getProfilePicUrl());

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

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User fetchedUser = userService.getUserById(1L);

        Assertions.assertEquals(user.getId(),fetchedUser.getId());
        Assertions.assertEquals(user.getFullName(),fetchedUser.getFullName());
        Assertions.assertEquals(user.getUsername(), fetchedUser.getUsername());
        Assertions.assertEquals(user.getProfilePicUrl(),fetchedUser.getProfilePicUrl());

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

        List<User> users = List.of(user);

        Mockito.when(userRepository.findAll()).thenReturn(users);

        List<User> allUsers = userService.getAllUsers();

        Assertions.assertEquals(1,allUsers.toArray().length);
    }

    @Test
    void updateUserWhenUserExists() {
        Long userId = 1L;
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setUsername("asma_123");
        existingUser.setFullName("Asma Mulla");

        UserRequest updatedUser = new UserRequest();
        updatedUser.setUsername("asma_456");
        updatedUser.setFullName("Asma Khan");

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenAnswer(i -> i.getArgument(0));

        User user = userService.updateUser(userId, updatedUser);

        Assertions.assertEquals("asma_456", user.getUsername());
        Assertions.assertEquals("Asma Khan", user.getFullName());

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