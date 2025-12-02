package com.projects.My_Instagram.services;

import com.projects.My_Instagram.DTOs.response.FollowRequestResponse;
import com.projects.My_Instagram.DTOs.response.UserResponse;
import com.projects.My_Instagram.constants.exception.ExceptionMessages;
import com.projects.My_Instagram.constants.response.ResponseMessages;
import com.projects.My_Instagram.exceptions.AppException;
import com.projects.My_Instagram.helper.Helper;
import com.projects.My_Instagram.helper.UserUtils;
import com.projects.My_Instagram.models.FollowRequest;
import com.projects.My_Instagram.models.FollowRequestStatus;
import com.projects.My_Instagram.models.User;
import com.projects.My_Instagram.repositories.FollowRequestRepository;
import com.projects.My_Instagram.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    public ResponseEntity<String> followUser(String username) {
        User currectUser = userUtils.fetchCurrectUser();
        User user = userUtils.fetchUser(username);

        if (currectUser.getId().equals(user.getId())) {
            throw new AppException(ExceptionMessages.FOLLOW_SELF.getMessage(), HttpStatus.BAD_REQUEST);
        }

        if (currectUser.getFollowing().contains(user)) {
            throw new AppException(ExceptionMessages.ALREADY_FOLLOWING.getMessage(), HttpStatus.CONFLICT);
        }

        if (user.getPrivateAccount()) {
            return sendFollowRequest(currectUser, user);
        }

        currectUser.getFollowing().add(user);
        user.getFollowers().add(currectUser);

        userRepository.save(currectUser);
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseMessages.FOLLOW_SUCCESS.getMessage());
    }

    private ResponseEntity<String> sendFollowRequest(User requester, User targetUser) {
        boolean alreadyRequested = followRequestRepository
                .existsByRequesterAndTargetUserAndStatus(
                        requester,
                        targetUser,
                        FollowRequestStatus.PENDING);


        if (alreadyRequested) {
            throw new AppException(ExceptionMessages.ALREADY_REQUESTED.getMessage(), HttpStatus.CONFLICT);
        }

        FollowRequest request = new FollowRequest();
        request.setRequester(requester);
        request.setTargetUser(targetUser);
        request.setStatus(FollowRequestStatus.PENDING);
        request.setRequestedAt(new Date());

        followRequestRepository.save(request);

        return  ResponseEntity.status(HttpStatus.CREATED).body(ResponseMessages.FOLLOW_REQUEST_SENT.getMessage());
    }

    public ResponseEntity<String> unfollowUser(String username) {
        User currectUser = userUtils.fetchCurrectUser();
        User user = userUtils.fetchUser(username);

        if (!currectUser.getFollowing().contains(user)) {
            throw new AppException(ExceptionMessages.NOT_FOLLOWING.getMessage(), HttpStatus.NOT_FOUND);
        }

        currectUser.getFollowing().remove(user);
        user.getFollowers().remove(currectUser);

        userRepository.save(currectUser);
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.OK).body(ResponseMessages.UNFOLLOW_SUCCESS.getMessage());
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
            followRequests.add(new FollowRequestResponse(followRequest.getRequester().getUsername(), followRequest.getRequestedAt()));
        }

        return followRequests;
    }

    public ResponseEntity<String> acceptFollowRequest(String username) {
        User currectUser = userUtils.fetchCurrectUser();
        List<FollowRequest> followRequests = followRequestRepository.findByTargetUserAndStatus(currectUser, FollowRequestStatus.PENDING);

        FollowRequest followRequest = findFollowRequest(username, followRequests);

        if (followRequest == null)
            throw new AppException(ExceptionMessages.FOLLOW_REQUEST_NOT_FOUND.getMessage(), HttpStatus.NOT_FOUND);

        User requester = followRequest.getRequester();
        currectUser.getFollowers().add(requester);
        requester.getFollowing().add(currectUser);

        userRepository.save(currectUser);
        userRepository.save(requester);

        followRequestRepository.delete(followRequest);

        return ResponseEntity.status(HttpStatus.OK).body(ResponseMessages.FOLLOW_REQUEST_ACCEPTED.getMessage());
    }

    public ResponseEntity<String> rejectFollowRequest(String username) {
        User currectUser = userUtils.fetchCurrectUser();
        List<FollowRequest> followRequests = followRequestRepository.findByTargetUserAndStatus(currectUser, FollowRequestStatus.PENDING);

        FollowRequest followRequest = findFollowRequest(username, followRequests);

        if (followRequest == null)
            throw new AppException(ExceptionMessages.FOLLOW_REQUEST_NOT_FOUND.getMessage(), HttpStatus.NOT_FOUND);

        followRequestRepository.delete(followRequest);

        return ResponseEntity.status(HttpStatus.OK).body(ResponseMessages.FOLLOW_REQUEST_REJECTED.getMessage());
    }

    private  FollowRequest findFollowRequest(String username, List<FollowRequest> followRequests) {

        return followRequests
                .stream()
                .filter(req -> req.getRequester().getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }
}
