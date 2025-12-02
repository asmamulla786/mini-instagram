package com.projects.My_Instagram.services;

import com.projects.My_Instagram.DTOs.response.FollowRequestResponse;
import com.projects.My_Instagram.DTOs.response.UserResponse;
import com.projects.My_Instagram.exceptions.AppException;
import com.projects.My_Instagram.helper.UserUtils;
import com.projects.My_Instagram.models.FollowRequest;
import com.projects.My_Instagram.models.FollowRequestStatus;
import com.projects.My_Instagram.models.User;
import com.projects.My_Instagram.repositories.FollowRequestRepository;
import com.projects.My_Instagram.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static com.projects.My_Instagram.constants.exception.ExceptionMessages.*;
import com.projects.My_Instagram.constants.response.ResponseMessages;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @Mock
    private UserUtils userUtils;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FollowRequestRepository followRequestRepository;

    @InjectMocks
    private FollowService followService;

    private User currentUser;
    private User targetUser;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("current_user");
        currentUser.setFollowing(new HashSet<>());
        currentUser.setFollowers(new HashSet<>());

        targetUser = new User();
        targetUser.setId(2L);
        targetUser.setUsername("target_user");
        targetUser.setFollowing(new HashSet<>());
        targetUser.setFollowers(new HashSet<>());
    }

    @Test
    void followUser_Success_PublicAccount() {
        targetUser.setPrivateAccount(false);
        when(userUtils.fetchCurrectUser()).thenReturn(currentUser);
        when(userUtils.fetchUser("target_user")).thenReturn(targetUser);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = followService.followUser("target_user");

        assertEquals(201, response.getStatusCodeValue());
        assertEquals(ResponseMessages.FOLLOW_SUCCESS.getMessage(), response.getBody());
        assertTrue(currentUser.getFollowing().contains(targetUser));
        assertTrue(targetUser.getFollowers().contains(currentUser));
        verify(userRepository, times(2)).save(any(User.class));
    }

    @Test
    void followUser_Success_PrivateAccount_SendsRequest() {
        targetUser.setPrivateAccount(true);
        when(userUtils.fetchCurrectUser()).thenReturn(currentUser);
        when(userUtils.fetchUser("target_user")).thenReturn(targetUser);
        when(followRequestRepository.existsByRequesterAndTargetUserAndStatus(
                currentUser, targetUser, FollowRequestStatus.PENDING)).thenReturn(false);
        when(followRequestRepository.save(any(FollowRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = followService.followUser("target_user");

        assertEquals(201, response.getStatusCodeValue());
        assertEquals(ResponseMessages.FOLLOW_REQUEST_SENT.getMessage(), response.getBody());
        verify(followRequestRepository, times(1)).save(any(FollowRequest.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void followUser_ThrowsException_WhenFollowingSelf() {
        when(userUtils.fetchCurrectUser()).thenReturn(currentUser);
        when(userUtils.fetchUser("current_user")).thenReturn(currentUser);

        AppException exception = assertThrows(AppException.class, () ->
                followService.followUser("current_user"));

        assertEquals(FOLLOW_SELF.getMessage(), exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void followUser_ThrowsException_WhenAlreadyFollowing() {
        targetUser.setPrivateAccount(false);
        currentUser.getFollowing().add(targetUser);
        when(userUtils.fetchCurrectUser()).thenReturn(currentUser);
        when(userUtils.fetchUser("target_user")).thenReturn(targetUser);

        AppException exception = assertThrows(AppException.class, () ->
                followService.followUser("target_user"));

        assertEquals(ALREADY_FOLLOWING.getMessage(), exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void followUser_ThrowsException_WhenRequestAlreadySent() {
        targetUser.setPrivateAccount(true);
        when(userUtils.fetchCurrectUser()).thenReturn(currentUser);
        when(userUtils.fetchUser("target_user")).thenReturn(targetUser);
        when(followRequestRepository.existsByRequesterAndTargetUserAndStatus(
                currentUser, targetUser, FollowRequestStatus.PENDING)).thenReturn(true);

        AppException exception = assertThrows(AppException.class, () ->
                followService.followUser("target_user"));

        assertEquals(ALREADY_REQUESTED.getMessage(), exception.getMessage());
        verify(followRequestRepository, never()).save(any(FollowRequest.class));
    }

    @Test
    void unfollowUser_Success() {
        currentUser.getFollowing().add(targetUser);
        targetUser.getFollowers().add(currentUser);
        when(userUtils.fetchCurrectUser()).thenReturn(currentUser);
        when(userUtils.fetchUser("target_user")).thenReturn(targetUser);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = followService.unfollowUser("target_user");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(ResponseMessages.UNFOLLOW_SUCCESS.getMessage(), response.getBody());
        assertFalse(currentUser.getFollowing().contains(targetUser));
        assertFalse(targetUser.getFollowers().contains(currentUser));
        verify(userRepository, times(2)).save(any(User.class));
    }

    @Test
    void unfollowUser_ThrowsException_WhenNotFollowing() {
        when(userUtils.fetchCurrectUser()).thenReturn(currentUser);
        when(userUtils.fetchUser("target_user")).thenReturn(targetUser);

        AppException exception = assertThrows(AppException.class, () ->
                followService.unfollowUser("target_user"));

        assertEquals(NOT_FOLLOWING.getMessage(), exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getFollowers_Success() {
        User follower1 = new User();
        follower1.setUsername("follower1");
        User follower2 = new User();
        follower2.setUsername("follower2");
        currentUser.setFollowers(new HashSet<>(Arrays.asList(follower1, follower2)));

        when(userUtils.fetchCurrectUser()).thenReturn(currentUser);

        List<UserResponse> followers = followService.getFollowers();

        assertEquals(2, followers.size());
        verify(userUtils, times(1)).fetchCurrectUser();
    }

    @Test
    void getFollowing_Success() {
        User following1 = new User();
        following1.setUsername("following1");
        User following2 = new User();
        following2.setUsername("following2");
        currentUser.setFollowing(new HashSet<>(Arrays.asList(following1, following2)));

        when(userUtils.fetchCurrectUser()).thenReturn(currentUser);

        List<UserResponse> following = followService.getFollowing();

        assertEquals(2, following.size());
        verify(userUtils, times(1)).fetchCurrectUser();
    }

    @Test
    void getAllFollowRequests_Success() {
        FollowRequest request1 = new FollowRequest();
        request1.setRequester(targetUser);
        request1.setRequestedAt(new Date());

        User requester2 = new User();
        requester2.setUsername("requester2");
        FollowRequest request2 = new FollowRequest();
        request2.setRequester(requester2);
        request2.setRequestedAt(new Date());

        List<FollowRequest> requests = Arrays.asList(request1, request2);

        when(userUtils.fetchCurrectUser()).thenReturn(currentUser);
        when(followRequestRepository.findByTargetUserAndStatus(
                currentUser, FollowRequestStatus.PENDING)).thenReturn(requests);

        List<FollowRequestResponse> followRequests = followService.getAllFollowRequests();

        assertEquals(2, followRequests.size());
        verify(followRequestRepository, times(1))
                .findByTargetUserAndStatus(currentUser, FollowRequestStatus.PENDING);
    }

    @Test
    void acceptFollowRequest_Success() {
        FollowRequest followRequest = new FollowRequest();
        followRequest.setRequester(targetUser);
        followRequest.setTargetUser(currentUser);
        followRequest.setStatus(FollowRequestStatus.PENDING);

        List<FollowRequest> requests = Collections.singletonList(followRequest);

        when(userUtils.fetchCurrectUser()).thenReturn(currentUser);
        when(followRequestRepository.findByTargetUserAndStatus(
                currentUser, FollowRequestStatus.PENDING)).thenReturn(requests);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = followService.acceptFollowRequest("target_user");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(ResponseMessages.FOLLOW_REQUEST_ACCEPTED.getMessage(), response.getBody());
        assertTrue(currentUser.getFollowers().contains(targetUser));
        assertTrue(targetUser.getFollowing().contains(currentUser));
        verify(followRequestRepository, times(1)).delete(followRequest);
        verify(userRepository, times(2)).save(any(User.class));
    }

    @Test
    void acceptFollowRequest_ThrowsException_WhenRequestNotFound() {
        when(userUtils.fetchCurrectUser()).thenReturn(currentUser);
        when(followRequestRepository.findByTargetUserAndStatus(
                currentUser, FollowRequestStatus.PENDING)).thenReturn(Collections.emptyList());

        AppException exception = assertThrows(AppException.class, () ->
                followService.acceptFollowRequest("target_user"));

        assertEquals(FOLLOW_REQUEST_NOT_FOUND.getMessage(), exception.getMessage());
        verify(followRequestRepository, never()).delete(any(FollowRequest.class));
    }

    @Test
    void rejectFollowRequest_Success() {
        FollowRequest followRequest = new FollowRequest();
        followRequest.setRequester(targetUser);
        followRequest.setTargetUser(currentUser);
        followRequest.setStatus(FollowRequestStatus.PENDING);

        List<FollowRequest> requests = Collections.singletonList(followRequest);

        when(userUtils.fetchCurrectUser()).thenReturn(currentUser);
        when(followRequestRepository.findByTargetUserAndStatus(
                currentUser, FollowRequestStatus.PENDING)).thenReturn(requests);

        var response = followService.rejectFollowRequest("target_user");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(ResponseMessages.FOLLOW_REQUEST_REJECTED.getMessage(), response.getBody());
        verify(followRequestRepository, times(1)).delete(followRequest);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void rejectFollowRequest_ThrowsException_WhenRequestNotFound() {
        when(userUtils.fetchCurrectUser()).thenReturn(currentUser);
        when(followRequestRepository.findByTargetUserAndStatus(
                currentUser, FollowRequestStatus.PENDING)).thenReturn(Collections.emptyList());

        AppException exception = assertThrows(AppException.class, () ->
                followService.rejectFollowRequest("target_user"));

        assertEquals(FOLLOW_REQUEST_NOT_FOUND.getMessage(), exception.getMessage());
        verify(followRequestRepository, never()).delete(any(FollowRequest.class));
    }
}

