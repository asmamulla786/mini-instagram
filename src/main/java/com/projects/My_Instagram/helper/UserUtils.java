package com.projects.My_Instagram.helper;

import com.projects.My_Instagram.exceptions.UserNotFoundException;
import com.projects.My_Instagram.models.User;
import com.projects.My_Instagram.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.projects.My_Instagram.constants.exception.ExceptionMessages.USER_NOT_FOUND;

@Component
public class UserUtils {
    private final UserRepository userRepository;

    public UserUtils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User fetchCurrectUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> user = userRepository.findByUsername(username);
        return user.get();
    }

    public User fetchUser(String username){
        Optional<User> user = userRepository.findByUsername(username);

        if(user.isEmpty()){
            throw new UserNotFoundException(USER_NOT_FOUND.getMessage());
        }

        return user.get();
    }
}
