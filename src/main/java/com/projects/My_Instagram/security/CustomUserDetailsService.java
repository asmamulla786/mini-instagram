package com.projects.My_Instagram.security;

import com.projects.My_Instagram.exceptions.UserNotFoundException;
import com.projects.My_Instagram.models.User;
import com.projects.My_Instagram.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static com.projects.My_Instagram.constants.exception.ExceptionMessages.*;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND.getMessage()));

        return new CustomUserDetails(user);
    }
}
