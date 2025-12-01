package com.projects.My_Instagram.controllers;

import com.projects.My_Instagram.DTOs.response.UserResponse;
import com.projects.My_Instagram.services.FollowService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class FollowController {
    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping("/{username}/follow")
    public String followUser(@PathVariable String username){
        return followService.followUser(username);
    }

    @DeleteMapping("/{username}/unfollow")
    public String unfollowUser(@PathVariable String username){
        return followService.unfollowUser(username);
    }

    @GetMapping("/followers")
    public List<UserResponse> getFollowers(){
        return followService.getFollowers();
    }

    @GetMapping("/following")
    public List<UserResponse> getFollowing(){
        return followService.getFollowing();
    }
}
