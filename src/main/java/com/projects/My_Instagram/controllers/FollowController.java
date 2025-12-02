package com.projects.My_Instagram.controllers;

import com.projects.My_Instagram.DTOs.response.FollowRequestResponse;
import com.projects.My_Instagram.DTOs.response.UserResponse;
import com.projects.My_Instagram.services.FollowService;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<String> followUser(@PathVariable String username){
        return followService.followUser(username);
    }

    @DeleteMapping("/{username}/unfollow")
    public ResponseEntity<String> unfollowUser(@PathVariable String username){
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

    @GetMapping("/follow-requests")
    public List<FollowRequestResponse> getAllFollowRequests(){
        return followService.getAllFollowRequests();
    }

    @PatchMapping("/follow/{username}/accept")
    public ResponseEntity<String> acceptFollowRequest(@PathVariable String username){
        return followService.acceptFollowRequest(username);
    }

    @PatchMapping("/follow/{username}/reject")
    public ResponseEntity<String> rejectFollowRequest(@PathVariable String username){
        return followService.rejectFollowRequest(username);
    }
}
