package com.projects.My_Instagram.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projects.My_Instagram.DTOs.response.FollowRequestResponse;
import com.projects.My_Instagram.DTOs.response.UserResponse;
import com.projects.My_Instagram.models.FollowRequest;
import com.projects.My_Instagram.models.FollowRequestStatus;
import com.projects.My_Instagram.models.Role;
import com.projects.My_Instagram.models.User;
import com.projects.My_Instagram.repositories.FollowRequestRepository;
import com.projects.My_Instagram.repositories.UserRepository;
import com.projects.My_Instagram.security.CustomUserDetails;
import com.projects.My_Instagram.jwt.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class FollowControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FollowRequestRepository followRequestRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private User currentUser;
    private User targetUser;
    private String authToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        followRequestRepository.deleteAll();

        currentUser = new User();
        currentUser.setUsername("currentuser");
        currentUser.setPassword(passwordEncoder.encode("password123"));
        currentUser.setFullName("Current User");
        currentUser.setRole(Role.USER);
        currentUser.setPrivateAccount(false);
        currentUser = userRepository.save(currentUser);

        targetUser = new User();
        targetUser.setUsername("targetuser");
        targetUser.setPassword(passwordEncoder.encode("password123"));
        targetUser.setFullName("Target User");
        targetUser.setRole(Role.USER);
        targetUser.setPrivateAccount(false);
        targetUser = userRepository.save(targetUser);

        authToken = jwtUtil.generateToken(new CustomUserDetails(currentUser));
    }

    @Test
    void followUser_Success_PublicAccount() throws Exception {
        String response = mockMvc.perform(post("/users/targetuser/follow")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertTrue(response.contains("successfully added to your following list"));

        User updatedCurrentUser = userRepository.findByUsername("currentuser").orElseThrow();
        User updatedTargetUser = userRepository.findByUsername("targetuser").orElseThrow();
        assertTrue(updatedCurrentUser.getFollowing().contains(updatedTargetUser));
        assertTrue(updatedTargetUser.getFollowers().contains(updatedCurrentUser));
    }

    @Test
    void followUser_Success_PrivateAccount_SendsRequest() throws Exception {
        targetUser.setPrivateAccount(true);
        userRepository.save(targetUser);

        String response = mockMvc.perform(post("/users/targetuser/follow")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertTrue(response.contains("Follow request sent successfully"));

        List<FollowRequest> requests = followRequestRepository
                .findByTargetUserAndStatus(targetUser, FollowRequestStatus.PENDING);
        assertEquals(1, requests.size());
        assertEquals(currentUser, requests.get(0).getRequester());
    }

    @Test
    void followUser_Fails_WhenFollowingSelf() throws Exception {
        mockMvc.perform(post("/users/currentuser/follow")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void followUser_Fails_WhenAlreadyFollowing() throws Exception {
        currentUser.getFollowing().add(targetUser);
        targetUser.getFollowers().add(currentUser);
        userRepository.save(currentUser);
        userRepository.save(targetUser);

        mockMvc.perform(post("/users/targetuser/follow")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isConflict());
    }

    @Test
    void unfollowUser_Success() throws Exception {
        currentUser.getFollowing().add(targetUser);
        targetUser.getFollowers().add(currentUser);
        userRepository.save(currentUser);
        userRepository.save(targetUser);

        String response = mockMvc.perform(delete("/users/targetuser/unfollow")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertTrue(response.contains("Unfollowed successfully"));

        User updatedCurrentUser = userRepository.findByUsername("currentuser").orElseThrow();
        assertFalse(updatedCurrentUser.getFollowing().contains(targetUser));
    }

    @Test
    void unfollowUser_Fails_WhenNotFollowing() throws Exception {
        mockMvc.perform(delete("/users/targetuser/unfollow")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void getFollowers_Success() throws Exception {
        User follower = new User();
        follower.setUsername("follower");
        follower.setPassword(passwordEncoder.encode("password123"));
        follower.setFullName("Follower User");
        follower.setRole(Role.USER);
        follower = userRepository.save(follower);

        currentUser.getFollowers().add(follower);
        userRepository.save(currentUser);

        String response = mockMvc.perform(get("/users/followers")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<UserResponse> followers = objectMapper.readValue(response,
                objectMapper.getTypeFactory().constructCollectionType(List.class, UserResponse.class));
        assertNotNull(followers);
        assertFalse(followers.isEmpty());
    }

    @Test
    void getFollowing_Success() throws Exception {
        currentUser.getFollowing().add(targetUser);
        userRepository.save(currentUser);

        String response = mockMvc.perform(get("/users/following")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<UserResponse> following = objectMapper.readValue(response,
                objectMapper.getTypeFactory().constructCollectionType(List.class, UserResponse.class));
        assertNotNull(following);
        assertFalse(following.isEmpty());
    }

    @Test
    void getAllFollowRequests_Success() throws Exception {
        targetUser.setPrivateAccount(true);
        userRepository.save(targetUser);

        FollowRequest request = new FollowRequest();
        request.setRequester(currentUser);
        request.setTargetUser(targetUser);
        request.setStatus(FollowRequestStatus.PENDING);
        followRequestRepository.save(request);

        String response = mockMvc.perform(get("/users/follow-requests")
                        .header("Authorization", "Bearer " + jwtUtil.generateToken(new CustomUserDetails(targetUser))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<FollowRequestResponse> requests = objectMapper.readValue(response,
                objectMapper.getTypeFactory().constructCollectionType(List.class, FollowRequestResponse.class));
        assertNotNull(requests);
        assertFalse(requests.isEmpty());
    }

    @Test
    void acceptFollowRequest_Success() throws Exception {
        targetUser.setPrivateAccount(true);
        userRepository.save(targetUser);

        FollowRequest request = new FollowRequest();
        request.setRequester(currentUser);
        request.setTargetUser(targetUser);
        request.setStatus(FollowRequestStatus.PENDING);
        followRequestRepository.save(request);

        String targetToken = jwtUtil.generateToken(new CustomUserDetails(targetUser));

        String response = mockMvc.perform(patch("/users/follow/currentuser/accept")
                        .header("Authorization", "Bearer " + targetToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertTrue(response.contains("Follow Request Accepted successfully"));

        User updatedCurrentUser = userRepository.findByUsername("currentuser").orElseThrow();
        User updatedTargetUser = userRepository.findByUsername("targetuser").orElseThrow();
        assertTrue(updatedCurrentUser.getFollowing().contains(updatedTargetUser));
        assertTrue(updatedTargetUser.getFollowers().contains(updatedCurrentUser));
    }

    @Test
    void rejectFollowRequest_Success() throws Exception {
        targetUser.setPrivateAccount(true);
        userRepository.save(targetUser);

        FollowRequest request = new FollowRequest();
        request.setRequester(currentUser);
        request.setTargetUser(targetUser);
        request.setStatus(FollowRequestStatus.PENDING);
        followRequestRepository.save(request);

        String targetToken = jwtUtil.generateToken(new CustomUserDetails(targetUser));

        String response = mockMvc.perform(patch("/users/follow/currentuser/reject")
                        .header("Authorization", "Bearer " + targetToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertTrue(response.contains("Follow Request Rejected successfully"));

        List<FollowRequest> requests = followRequestRepository
                .findByTargetUserAndStatus(targetUser, FollowRequestStatus.PENDING);
        assertTrue(requests.isEmpty());
    }
}

