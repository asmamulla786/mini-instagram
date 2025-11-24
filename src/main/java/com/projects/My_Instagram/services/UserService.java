package com.projects.My_Instagram.services;

import com.projects.My_Instagram.DTOs.UserRequest;
import com.projects.My_Instagram.exceptions.UserNameExistsException;
import com.projects.My_Instagram.exceptions.UserNameNullException;
import com.projects.My_Instagram.exceptions.UserNotFoundException;
import com.projects.My_Instagram.repositories.UserRepository;
import com.projects.My_Instagram.models.User;
import org.springframework.stereotype.Service;
import java.util.List;

import static com.projects.My_Instagram.Constants.ExceptionMessages.*;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(UserRequest newUser) {
        User user = new User();
        setUserName(user, newUser.getUsername());
        user.setFullName(newUser.getFullName());
        user.setProfilePicUrl(newUser.getProfilePicUrl());

        return userRepository.save(user);
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

    public User getUserById(Long id) {
        return getUser(id);
    }

    private User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND.getMessage()));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(Long id, UserRequest updatedUser) {
        User existingUser = getUser(id);

        setUserName(existingUser, updatedUser.getUsername());
        existingUser.setFullName(updatedUser.getFullName());
        existingUser.setProfilePicUrl(updatedUser.getProfilePicUrl());

        return userRepository.save(existingUser);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND.getMessage()));
        userRepository.delete(user);
    }

}
