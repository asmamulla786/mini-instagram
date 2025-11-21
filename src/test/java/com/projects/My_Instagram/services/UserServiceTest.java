package com.projects.My_Instagram.services;

import com.projects.My_Instagram.Constants.ExceptionMessages;
import com.projects.My_Instagram.DTOs.UserRequest;
import com.projects.My_Instagram.exceptions.UserNameExistsException;
import com.projects.My_Instagram.models.User;
import com.projects.My_Instagram.repositories.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

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

        Assertions.assertEquals(ExceptionMessages.USER_NAME_EXISTS.getMessage(), userNameExistsException.getMessage());
        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any(User.class));
    }

    @Test
    void getUserById() {
    }

    @Test
    void getAllUsers() {
    }

    @Test
    void updateUser() {
    }

    @Test
    void deleteUser() {
    }
}