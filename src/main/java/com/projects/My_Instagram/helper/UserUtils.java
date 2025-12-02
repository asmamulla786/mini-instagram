package com.projects.My_Instagram.helper;

import com.projects.My_Instagram.exceptions.PostNotFoundException;
import com.projects.My_Instagram.exceptions.UserNotFoundException;
import com.projects.My_Instagram.models.Post;
import com.projects.My_Instagram.models.User;
import com.projects.My_Instagram.repositories.PostRepository;
import com.projects.My_Instagram.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.projects.My_Instagram.constants.exception.ExceptionMessages.POST_NOT_FOUND;
import static com.projects.My_Instagram.constants.exception.ExceptionMessages.USER_NOT_FOUND;

@Component
public class UserUtils {
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public UserUtils(UserRepository userRepository, PostRepository postRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
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

    public Post fetchPost(Long post_id) {
        Optional<Post> post = postRepository.findById(post_id);

        if(post.isEmpty()){
            throw new PostNotFoundException(POST_NOT_FOUND.getMessage());
        }

        return post.get();
    }
}
