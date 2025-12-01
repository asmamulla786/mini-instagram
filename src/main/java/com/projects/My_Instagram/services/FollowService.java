package com.projects.My_Instagram.services;

import com.projects.My_Instagram.DTOs.response.FollowRequestResponse;
import com.projects.My_Instagram.DTOs.response.UserResponse;
import com.projects.My_Instagram.helper.Helper;
import com.projects.My_Instagram.helper.UserUtils;
import com.projects.My_Instagram.models.FollowRequest;
import com.projects.My_Instagram.models.FollowRequestStatus;
import com.projects.My_Instagram.models.User;
import com.projects.My_Instagram.repositories.FollowRequestRepository;
import com.projects.My_Instagram.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class FollowService {
    private final UserUtils userUtils;
    private final UserRepository userRepository;
    private final FollowRequestRepository followRequestRepository;

    public FollowService(UserUtils userUtils, UserRepository userRepository, FollowRequestRepository followRequestRepository) {
        this.userUtils = userUtils;
        this.userRepository = userRepository;
        this.followRequestRepository = followRequestRepository;
    }

    public String followUser(String username) {
        User currectUser = userUtils.fetchCurrectUser();
        User user = userUtils.fetchUser(username);

        if (currectUser.getId().equals(user.getId())) {
            return "You can't follow yourself";   //todo - Need to extract strings to class
        }

        if (currectUser.getFollowing().contains(user)) {
            return "You are already following the user";  //todo - Need to extract strings to class
        }

        if (user.getPrivateAccount()) {
            return sendFollowRequest(currectUser, user);
        }
        currectUser.getFollowing().add(user);
        user.getFollowers().add(currectUser);

        userRepository.save(currectUser);
        userRepository.save(user);

        return "User successfully added to your following list";  //todo - Need to extract strings to class
    }

    private String sendFollowRequest(User requester, User targetUser) {
        boolean alreadyRequested = followRequestRepository
                .existsByRequesterAndTargetUserAndStatus(
                        requester,
                        targetUser,
                        FollowRequestStatus.PENDING);


        if(alreadyRequested){
            return "You already requested";  //todo - Need to extract strings to class
        }
        FollowRequest request = new FollowRequest();
        request.setRequester(requester);
        request.setTargetUser(targetUser);
        request.setStatus(FollowRequestStatus.PENDING);
        request.setRequestedAt(new Date());

        followRequestRepository.save(request);

        return "Follow request sent to " + targetUser.getUsername();  //todo - Need to extract strings to class
    }

    public String unfollowUser(String username) {
        User currectUser = userUtils.fetchCurrectUser();
        User user = userUtils.fetchUser(username);

        if (!currectUser.getFollowing().contains(user)) {
            return "You are not following the user";  //todo - Need to extract strings to class
        }

        currectUser.getFollowing().remove(user);
        user.getFollowers().remove(currectUser);

        userRepository.save(currectUser);
        userRepository.save(user);

        return "Unfollowed successfully";  //todo - Need to extract strings to class
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

    public List<FollowRequestResponse> getAllFollowRequests() {
        User user = userUtils.fetchCurrectUser();
        List<FollowRequestResponse> followRequests = new ArrayList<>();
        for (FollowRequest followRequest : followRequestRepository.findByTargetUserAndStatus(user, FollowRequestStatus.PENDING)) {
            followRequests.add(new FollowRequestResponse(followRequest.getRequester().getUsername(),followRequest.getRequestedAt()));
        }

        return followRequests;
    }
}
