package com.projects.My_Instagram.services;

import com.projects.My_Instagram.DTOs.response.UserResponse;
import com.projects.My_Instagram.exceptions.InvalidCredentialsException;
import com.projects.My_Instagram.exceptions.UserNotFoundException;
import com.projects.My_Instagram.helper.Helper;
import com.projects.My_Instagram.helper.UserUtils;
import com.projects.My_Instagram.models.User;
import com.projects.My_Instagram.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import static com.projects.My_Instagram.constants.exception.ExceptionMessages.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FollowService {
    private final UserUtils userUtils;
    private final UserRepository userRepository;

    public FollowService(UserUtils userUtils, UserRepository userRepository) {
        this.userUtils = userUtils;
        this.userRepository = userRepository;
    }

    public String followUser(String username) {
        User currectUser = userUtils.fetchCurrectUser();
        User user = userUtils.fetchUser(username);

        if (currectUser.getId().equals(user.getId())) {
            return "You can't follow yourself";
        }

        if (currectUser.getFollowing().contains(user)) {
            return "You are already following the user";
        }

        currectUser.getFollowing().add(user);
        user.getFollowers().add(currectUser);

        userRepository.save(currectUser);
        userRepository.save(user);

        return "User successfully added to your following list";
    }

    public String unfollowUser(String username) {
        User currectUser = userUtils.fetchCurrectUser();
        User user = userUtils.fetchUser(username);

        if(!currectUser.getFollowing().contains(user)){
           return "You are not following the user";
        }

        currectUser.getFollowing().remove(user);
        user.getFollowers().remove(currectUser);

        userRepository.save(currectUser);
        userRepository.save(user);

        return "Unfollowed successfully";
    }

    public List<UserResponse> getFollowers() {
        User user = userUtils.fetchCurrectUser();
        List<UserResponse> followers = new ArrayList<>();
        for (User follower : user.getFollowers()) {
            followers.add(Helper.formUserResponse(follower));
        }

        return followers;
    }

    public List<UserResponse> getFollowing() {
        User user = userUtils.fetchCurrectUser();
        List<UserResponse> following = new ArrayList<>();
        for (User follower : user.getFollowing()) {
            following.add(Helper.formUserResponse(follower));
        }

        return following;
    }
}
