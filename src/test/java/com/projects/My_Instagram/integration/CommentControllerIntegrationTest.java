package com.projects.My_Instagram.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projects.My_Instagram.DTOs.request.CreateCommentRequest;
import com.projects.My_Instagram.DTOs.response.CommentResponse;
import com.projects.My_Instagram.models.Comment;
import com.projects.My_Instagram.models.Post;
import com.projects.My_Instagram.models.Role;
import com.projects.My_Instagram.models.User;
import com.projects.My_Instagram.repositories.CommentRepository;
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
class CommentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private String authToken;
    private Post testPost;
    private Comment testComment;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        postRepository.deleteAll();
        commentRepository.deleteAll();

        User testUser = new User();
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

        testComment = new Comment();
        testComment.setContent("Test Comment");
        testComment.setCommentedUser(testUser);
        testComment.setCommentedPost(testPost);
        testComment = commentRepository.save(testComment);
    }

    @Test
    void createComment_Success() throws Exception {
        String commentJson = "{\"content\":\"New Comment\"}";

        String response = mockMvc.perform(post("/posts/" + testPost.getId() + "/comments")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CommentResponse commentResponse = objectMapper.readValue(response, CommentResponse.class);
        assertNotNull(commentResponse);
        assertEquals("New Comment", commentResponse.comment());
        assertEquals("testuser", commentResponse.username());
    }

    @Test
    void createComment_Fails_WhenPostNotFound() throws Exception {
        String commentJson = "{\"content\":\"New Comment\"}";

        mockMvc.perform(post("/posts/999/comments")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllComments_Success() throws Exception {
        String response = mockMvc.perform(get("/posts/" + testPost.getId() + "/comments")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<CommentResponse> comments = objectMapper.readValue(response,
                objectMapper.getTypeFactory().constructCollectionType(List.class, CommentResponse.class));
        assertNotNull(comments);
        assertFalse(comments.isEmpty());
        assertEquals("Test Comment", comments.get(0).comment());
    }

    @Test
    void getAllComments_Fails_WhenPostNotFound() throws Exception {
        mockMvc.perform(get("/posts/999/comments")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteComment_Success() throws Exception {
        Long commentId = testComment.getId();
        
        String response = mockMvc.perform(delete("/comments/" + commentId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertTrue(response.contains("Deleted comment successfully"));
        assertFalse(commentRepository.findById(commentId).isPresent());
    }

    @Test
    void deleteComment_Fails_WhenNotOwner() throws Exception {
        User otherUser = new User();
        otherUser.setUsername("otheruser");
        otherUser.setPassword(passwordEncoder.encode("password123"));
        otherUser.setFullName("Other User");
        otherUser.setRole(Role.USER);
        otherUser = userRepository.save(otherUser);

        String otherToken = jwtUtil.generateToken(new CustomUserDetails(otherUser));

        mockMvc.perform(delete("/comments/" + testComment.getId())
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteComment_Fails_WhenCommentNotFound() throws Exception {
        mockMvc.perform(delete("/comments/999")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }
}

