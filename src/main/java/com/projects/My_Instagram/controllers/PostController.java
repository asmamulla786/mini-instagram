package com.projects.My_Instagram.controllers;

import com.projects.My_Instagram.DTOs.PostRequest;
import com.projects.My_Instagram.models.Post;
import com.projects.My_Instagram.services.PostService;
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
    public Post createPost(@RequestBody PostRequest post){
        return postService.createPost(post);
    }

    @GetMapping("/user/{username}")
    public List<Post> getAllPostsOfUser(@PathVariable String username){
        return postService.getAllPostOfUser(username);
    }

    @DeleteMapping("/{post_id}")
    public void deletePost(@PathVariable Long post_id){
        postService.deletePost(post_id);
    }

    @DeleteMapping("/user/{username}")
    public void deleteAllPostsOfUser(@PathVariable String username){
        postService.deleteAllPostOfUser(username);
    }
}
