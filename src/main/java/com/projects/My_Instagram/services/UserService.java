package com.projects.My_Instagram.services;

import com.projects.My_Instagram.DTOs.request.UserRequest;
import com.projects.My_Instagram.DTOs.response.UserResponse;
import com.projects.My_Instagram.exceptions.PasswordNullException;
import com.projects.My_Instagram.exceptions.UserNameExistsException;
import com.projects.My_Instagram.exceptions.UserNameNullException;
import com.projects.My_Instagram.exceptions.UserNotFoundException;
import com.projects.My_Instagram.repositories.UserRepository;
import com.projects.My_Instagram.models.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.projects.My_Instagram.constants.exception.ExceptionMessages.*;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse createUser(UserRequest newUser) {
        User user = new User();
        setUserName(user, newUser.getUsername());
        setPassword(user,newUser.getPassword());
        user.setFullName(newUser.getFullName());
        user.setProfilePicUrl(newUser.getProfilePicUrl());


        User createdUser = userRepository.save(user);

        return formUserResponse(createdUser);
    }

    private UserResponse formUserResponse(User createdUser) {
        UserResponse userResponse = new UserResponse();
        userResponse.setId(createdUser.getId());
        userResponse.setFullName(createdUser.getFullName());
        userResponse.setProfilePicUrl(createdUser.getProfilePicUrl());
        userResponse.setUsername(createdUser.getUsername());

        return userResponse;
    }

    private void setPassword(User user, String password) {
        if(!StringUtils.hasText(password)){
            throw new PasswordNullException(PASS_WORD_NUll.getMessage());
        }

        String encodedPassword = passwordEncoder.encode(password);
        user.setPassword(encodedPassword);
    }

    private void setUserName(User user, String username) {
        if (username != null) {
            boolean exists = userRepository.existsByUsername(username);
            if (exists) {
                throw new UserNameExistsException(USER_NAME_EXISTS.getMessage());
            }
            user.setUsername(username);
            return;
        }

        throw new UserNameNullException(USER_NAME_NULL.getMessage());
    }

    public UserResponse getUserById(Long id) {
        User user = getUser(id);

        return formUserResponse(user);
    }

    private User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND.getMessage()));
    }

    public List<UserResponse> getAllUsers() {
        List<UserResponse> allUsers = new ArrayList<>();
        for (User user : userRepository.findAll()) {
            allUsers.add(formUserResponse(user));
        }

        return allUsers;
    }

    public UserResponse updateUser(Long id, UserRequest updatedUser) {
        User existingUser = getUser(id);

        setUserName(existingUser, updatedUser.getUsername());
        existingUser.setFullName(updatedUser.getFullName());
        existingUser.setProfilePicUrl(updatedUser.getProfilePicUrl());

        User user = userRepository.save(existingUser);
        return formUserResponse(user);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND.getMessage()));
        userRepository.delete(user);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public User findUserByUserName(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.get();
    }
}
