package com.projects.My_Instagram.services;

import com.projects.My_Instagram.DTOs.request.PostRequest;
import com.projects.My_Instagram.DTOs.response.PostResponse;
import com.projects.My_Instagram.exceptions.AccessDeniedException;
import com.projects.My_Instagram.exceptions.PostNotFoundException;
import com.projects.My_Instagram.exceptions.UserNotFoundException;
import com.projects.My_Instagram.models.Post;
import com.projects.My_Instagram.models.User;
import com.projects.My_Instagram.repositories.PostRepository;
import com.projects.My_Instagram.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.*;

import static com.projects.My_Instagram.constants.exception.ExceptionMessages.*;
import static com.projects.My_Instagram.constants.response.ResponseMessages.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PostService postService;

    private User currentUser;
    private Post post;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("current_user");
        currentUser.setLikedPosts(new HashSet<>());

        post = new Post();
        post.setId(1L);
        post.setCaption("Test caption");
        post.setImageUrl("http://example.com/image.jpg");
        post.setUser(currentUser);
        post.setLikedUsers(new HashSet<>());
        post.setUploadedAt(LocalDateTime.now());

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("current_user");
    }

    @Test
    void createPost_Success() {
        PostRequest request = new PostRequest();
        request.setCaption("Test caption");
        request.setImageUrl("http://example.com/image.jpg");

        when(userRepository.findByUsername("current_user")).thenReturn(Optional.of(currentUser));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });

        PostResponse response = postService.createPost(request);

        assertNotNull(response);
        assertEquals("Test caption", response.getCaption());
        assertEquals("http://example.com/image.jpg", response.getImageUrl());
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    void deletePost_Success() {
        when(userRepository.findByUsername("current_user")).thenReturn(Optional.of(currentUser));
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        postService.deletePost(1L);

        verify(postRepository, times(1)).deleteById(1L);
    }

    @Test
    void deletePost_ThrowsException_WhenPostNotFound() {
        when(userRepository.findByUsername("current_user")).thenReturn(Optional.of(currentUser));
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        PostNotFoundException exception = assertThrows(PostNotFoundException.class, () ->
                postService.deletePost(1L));

        assertEquals(POST_NOT_FOUND.getMessage(), exception.getMessage());
        verify(postRepository, never()).deleteById(anyLong());
    }

    @Test
    void deletePost_ThrowsException_WhenNotPostOwner() {
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("other_user");
        post.setUser(otherUser);

        when(userRepository.findByUsername("current_user")).thenReturn(Optional.of(currentUser));
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () ->
                postService.deletePost(1L));

        assertEquals(UNAUTHORIZED.getMessage(), exception.getMessage());
        verify(postRepository, never()).deleteById(anyLong());
    }

    @Test
    void getAllPostOfUser_Success() {
        Post post1 = new Post();
        post1.setId(1L);
        post1.setUser(currentUser);
        post1.setCaption("Post 1");

        Post post2 = new Post();
        post2.setId(2L);
        post2.setUser(currentUser);
        post2.setCaption("Post 2");

        List<Post> posts = Arrays.asList(post1, post2);

        when(userRepository.findByUsername("current_user")).thenReturn(Optional.of(currentUser));
        when(postRepository.findByUser(currentUser)).thenReturn(posts);

        List<PostResponse> allPosts = postService.getAllPostOfUser("current_user");

        assertEquals(2, allPosts.size());
        verify(userRepository, times(1)).findByUsername("current_user");
        verify(postRepository, times(1)).findByUser(currentUser);
    }

    @Test
    void getAllPostOfUser_ThrowsException_WhenUserNotFound() {
        when(userRepository.findByUsername("unknown_user")).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () ->
                postService.getAllPostOfUser("unknown_user"));

        assertEquals(USER_NOT_FOUND.getMessage(), exception.getMessage());
        verify(postRepository, never()).findByUser(any(User.class));
    }

    @Test
    void deleteAllPostOfUser_Success() {
        when(userRepository.findByUsername("current_user")).thenReturn(Optional.of(currentUser));

        postService.deleteAllPostOfUser();

        verify(userRepository, times(1)).findByUsername("current_user");
        verify(postRepository, times(1)).deleteByUser(currentUser);
    }

    @Test
    void likePost_Success() {
        when(userRepository.findByUsername("current_user")).thenReturn(Optional.of(currentUser));
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<String> response = postService.likePost(1L);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(LIKED_SUCCESSFULLY.getMessage(), response.getBody());
        assertTrue(post.getLikedUsers().contains(currentUser));
        verify(postRepository, times(1)).save(post);
    }

    @Test
    void likePost_ReturnsAlreadyLiked_WhenAlreadyLiked() {
        post.getLikedUsers().add(currentUser);
        currentUser.getLikedPosts().add(post);

        when(userRepository.findByUsername("current_user")).thenReturn(Optional.of(currentUser));
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        ResponseEntity<String> response = postService.likePost(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ALREADY_LIKED.getMessage(), response.getBody());
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void unlikePost_Success() {
        post.getLikedUsers().add(currentUser);
        currentUser.getLikedPosts().add(post);

        when(userRepository.findByUsername("current_user")).thenReturn(Optional.of(currentUser));
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<String> response = postService.unlikePost(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(UNLIKED_SUCCESSFULLY.getMessage(), response.getBody());
        assertFalse(post.getLikedUsers().contains(currentUser));
        verify(postRepository, times(1)).save(post);
    }

    @Test
    void unlikePost_ReturnsNotLiked_WhenNotLiked() {
        when(userRepository.findByUsername("current_user")).thenReturn(Optional.of(currentUser));
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        ResponseEntity<String> response = postService.unlikePost(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(NOT_LIKED.getMessage(), response.getBody());
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void likePost_ThrowsException_WhenPostNotFound() {
        when(userRepository.findByUsername("current_user")).thenReturn(Optional.of(currentUser));
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        PostNotFoundException exception = assertThrows(PostNotFoundException.class, () ->
                postService.likePost(1L));

        assertEquals(POST_NOT_FOUND.getMessage(), exception.getMessage());
        verify(postRepository, never()).save(any(Post.class));
    }
}
