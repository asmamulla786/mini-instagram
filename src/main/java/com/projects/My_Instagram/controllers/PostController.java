package com.projects.My_Instagram.controllers;

import com.projects.My_Instagram.DTOs.request.PostRequest;
import com.projects.My_Instagram.DTOs.response.PostResponse;
import com.projects.My_Instagram.services.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public PostResponse createPost(@RequestBody PostRequest post) {
        return postService.createPost(post);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping({"", "/user/{username}"})
    public List<PostResponse> getAllPostsOfUser(@PathVariable(required = false) String username) {
        if (username == null) {
            return postService.getAllPostOfUser(SecurityContextHolder.getContext().getAuthentication().getName());
        }

        return postService.getAllPostOfUser(username);
    }

    @DeleteMapping("/{post_id}")
    public void deletePost(@PathVariable Long post_id) {
        postService.deletePost(post_id);
    }

    @DeleteMapping
    public void deleteAllPostsOfUser() {
        postService.deleteAllPostOfUser();
    }

    @PostMapping("{post_id}/like")
    public ResponseEntity<String> likePost(@PathVariable Long post_id){
        return postService.likePost(post_id);
    }

    @DeleteMapping("{post_id}/unlike")
    public ResponseEntity<String> unlikePost(@PathVariable Long post_id){
        return postService.unlikePost(post_id);
    }
}
