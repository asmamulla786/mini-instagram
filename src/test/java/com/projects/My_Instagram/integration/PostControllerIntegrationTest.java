package com.projects.My_Instagram.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projects.My_Instagram.DTOs.request.PostRequest;
import com.projects.My_Instagram.DTOs.response.PostResponse;
import com.projects.My_Instagram.models.Post;
import com.projects.My_Instagram.models.Role;
import com.projects.My_Instagram.models.User;
import com.projects.My_Instagram.repositories.PostRepository;
import com.projects.My_Instagram.repositories.UserRepository;
import com.projects.My_Instagram.security.CustomUserDetails;
import com.projects.My_Instagram.jwt.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
class PostControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private User testUser;
    private String authToken;
    private Post testPost;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        postRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setFullName("Test User");
        testUser.setRole(Role.USER);
        testUser.setPrivateAccount(false);
        testUser = userRepository.save(testUser);

        authToken = jwtUtil.generateToken(new CustomUserDetails(testUser));

        testPost = new Post();
        testPost.setCaption("Test Caption");
        testPost.setImageUrl("http://example.com/image.jpg");
        testPost.setUser(testUser);
        testPost = postRepository.save(testPost);
    }

    @Test
    void createPost_Success() throws Exception {
        PostRequest postRequest = new PostRequest();
        postRequest.setCaption("New Post");
        postRequest.setImageUrl("http://example.com/new.jpg");

        String response = mockMvc.perform(post("/posts")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PostResponse postResponse = objectMapper.readValue(response, PostResponse.class);
        assertNotNull(postResponse);
        assertEquals("New Post", postResponse.getCaption());
        assertEquals("http://example.com/new.jpg", postResponse.getImageUrl());
    }

    @Test
    void createPost_Fails_WithoutAuthentication() throws Exception {
        PostRequest postRequest = new PostRequest();
        postRequest.setCaption("New Post");
        postRequest.setImageUrl("http://example.com/new.jpg");

        mockMvc.perform(post("/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllPostsOfUser_Success() throws Exception {
        String response = mockMvc.perform(get("/posts/user/testuser")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<PostResponse> posts = objectMapper.readValue(response,
                objectMapper.getTypeFactory().constructCollectionType(List.class, PostResponse.class));
        assertNotNull(posts);
        assertFalse(posts.isEmpty());
    }

    @Test
    void getAllPostsOfCurrentUser_Success() throws Exception {
        String response = mockMvc.perform(get("/posts")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<PostResponse> posts = objectMapper.readValue(response,
                objectMapper.getTypeFactory().constructCollectionType(List.class, PostResponse.class));
        assertNotNull(posts);
        assertFalse(posts.isEmpty());
    }

    @Test
    void deletePost_Success() throws Exception {
        mockMvc.perform(delete("/posts/" + testPost.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());

        assertFalse(postRepository.findById(testPost.getId()).isPresent());
    }

    @Test
    void deletePost_Fails_WhenNotOwner() throws Exception {
        User otherUser = new User();
        otherUser.setUsername("otheruser");
        otherUser.setPassword(passwordEncoder.encode("password123"));
        otherUser.setFullName("Other User");
        otherUser.setRole(Role.USER);
        otherUser = userRepository.save(otherUser);

        String otherToken = jwtUtil.generateToken(new CustomUserDetails(otherUser));

        mockMvc.perform(delete("/posts/" + testPost.getId())
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void likePost_Success() throws Exception {
        String response = mockMvc.perform(post("/posts/" + testPost.getId() + "/like")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertTrue(response.contains("liked successfully"));
    }

    @Test
    void unlikePost_Success() throws Exception {
        // First like the post
        testPost.getLikedUsers().add(testUser);
        testUser.getLikedPosts().add(testPost);
        postRepository.save(testPost);
        userRepository.save(testUser);

        String response = mockMvc.perform(delete("/posts/" + testPost.getId() + "/unlike")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertTrue(response.contains("unliked successfully"));
    }

    @Test
    void deleteAllPostsOfUser_Success() throws Exception {
        mockMvc.perform(delete("/posts")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());

        List<Post> userPosts = postRepository.findByUser(testUser);
        assertTrue(userPosts.isEmpty());
    }
}

