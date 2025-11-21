package com.projects.My_Instagram.services;

import com.projects.My_Instagram.DTOs.PostRequest;
import com.projects.My_Instagram.exceptions.PostNotFoundException;
import com.projects.My_Instagram.exceptions.UserNotFoundException;
import com.projects.My_Instagram.models.Post;
import com.projects.My_Instagram.models.User;
import com.projects.My_Instagram.repositories.PostRepository;
import com.projects.My_Instagram.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.projects.My_Instagram.Constants.ExceptionMessages.*;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND.getMessage()));
    }

    public Post createPost(PostRequest postRequest) throws RuntimeException {
        /* todo -> Create own exception classes */
        User user = getUser(postRequest.getUsername());

        Post post = new Post();
        post.setCaption(postRequest.getCaption());
        post.setImageUrl(postRequest.getImageUrl());
        post.setUploadedAt(LocalDateTime.now());
        post.setUser(user);

        return postRepository.save(post);
    }

    public void deletePost(Long post_id){
        /* todo -> Create own exception classes */
        Optional<Post> post = postRepository.findById(post_id);

        if(post.isEmpty()){
            throw new PostNotFoundException(POST_NOT_FOUND.getMessage());
        }

        postRepository.deleteById(post_id);
    }

    public List<Post> getAllPostOfUser(@PathVariable String username){
        User user = getUser(username);
        return postRepository.findByUser(user);
    }

    @Transactional
    public void deleteAllPostOfUser(@PathVariable String username){
        User user = getUser(username);
        postRepository.deleteByUser(user);
    }
}
