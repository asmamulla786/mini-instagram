package com.projects.My_Instagram.services;

import com.projects.My_Instagram.DTOs.request.PostRequest;
import com.projects.My_Instagram.DTOs.response.PostResponse;
import com.projects.My_Instagram.exceptions.AccessDeniedException;
import com.projects.My_Instagram.exceptions.PostNotFoundException;
import com.projects.My_Instagram.exceptions.UserNotFoundException;
import com.projects.My_Instagram.helper.Helper;
import com.projects.My_Instagram.models.Post;
import com.projects.My_Instagram.models.User;
import com.projects.My_Instagram.repositories.PostRepository;
import com.projects.My_Instagram.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.projects.My_Instagram.constants.exception.ExceptionMessages.*;
import static com.projects.My_Instagram.constants.response.ResponseMessages.*;

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

    public PostResponse createPost(PostRequest postRequest) throws RuntimeException {
     User currentUser = fetchCurrectUser();
        Post post = new Post();
        post.setCaption(postRequest.getCaption());
        post.setImageUrl(postRequest.getImageUrl());
        post.setUploadedAt(LocalDateTime.now());
        post.setUser(currentUser);

        return Helper.formPostResponse(postRepository.save(post));
    }

    private User fetchCurrectUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> user = userRepository.findByUsername(username);
        return user.get();
    }

    public void deletePost(Long post_id) {
        Post post = fetchPost(post_id);
        User currectUser = fetchCurrectUser();

        if(!Objects.equals(post.getUser().getUsername(), currectUser.getUsername())){
            throw new AccessDeniedException(UNAUTHORIZED.getMessage());
        }

        postRepository.deleteById(post_id);
    }

    private Post fetchPost(Long post_id) {
        Optional<Post> post = postRepository.findById(post_id);

        if(post.isEmpty()){
            throw new PostNotFoundException(POST_NOT_FOUND.getMessage());
        }

        Post post1 = post.get();
        return post1;
    }

    public List<PostResponse> getAllPostOfUser(String username){
        User user = getUser(username);
        List<PostResponse> allPosts = new ArrayList<>();
        for (Post post : postRepository.findByUser(user)) {
            allPosts.add(Helper.formPostResponse(post));
        }

        return allPosts;
    }

    @Transactional
    public void deleteAllPostOfUser(){
        User user = fetchCurrectUser();
        postRepository.deleteByUser(user);
    }

    public ResponseEntity<String> likePost(Long post_id){
        User currectUser = fetchCurrectUser();
        Post post = fetchPost(post_id);
        if (post.getLikedUsers().contains(currectUser)){
            return ResponseEntity.status(HttpStatus.OK).body(ALREADY_LIKED.getMessage());
        }

        post.getLikedUsers().add(currectUser);
        currectUser.getLikedPosts().add(post);

        postRepository.save(post);
        return ResponseEntity.status(HttpStatus.CREATED).body(LIKED_SUCCESSFULLY.getMessage());
    }

    public ResponseEntity<String> unlikePost(Long post_id){
        User currectUser = fetchCurrectUser();
        Post post = fetchPost(post_id);
        if (!post.getLikedUsers().contains(currectUser)){
            return ResponseEntity.status(HttpStatus.OK).body(NOT_LIKED.getMessage());
        }

        post.getLikedUsers().remove(currectUser);
        currectUser.getLikedPosts().remove(post);

        postRepository.save(post);
        return ResponseEntity.status(HttpStatus.OK).body(UNLIKED_SUCCESSFULLY.getMessage());
    }

}
