package com.projects.My_Instagram.services;

import com.projects.My_Instagram.DTOs.response.CommentResponse;
import com.projects.My_Instagram.exceptions.AccessDeniedException;
import com.projects.My_Instagram.exceptions.CommentNotFoundException;
import com.projects.My_Instagram.exceptions.PostNotFoundException;
import com.projects.My_Instagram.helper.UserUtils;
import com.projects.My_Instagram.models.Comment;
import com.projects.My_Instagram.models.Post;
import com.projects.My_Instagram.models.User;
import com.projects.My_Instagram.repositories.CommentRepository;
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

import java.util.*;

import static com.projects.My_Instagram.constants.exception.ExceptionMessages.*;
import static com.projects.My_Instagram.constants.response.ResponseMessages.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)

class CommentServiceTest {

    @Mock private CommentRepository commentRepository;
    @Mock private UserUtils userUtils;
    @Mock private UserRepository userRepository;
    @Mock private PostRepository postRepository;

    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private CommentService commentService;

    private User currentUser;
    private Post post;
    private Comment comment;

    @BeforeEach
    void setUp() {

        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("current_user");

        User postOwner = new User();
        postOwner.setId(2L);
        postOwner.setUsername("post_owner");

        post = new Post();
        post.setId(1L);
        post.setUser(postOwner);

        comment = new Comment();
        comment.setId(1L);
        comment.setContent("Test comment");
        comment.setCommentedUser(currentUser);
        comment.setCommentedPost(post);
        comment.setUploadedAt(new Date());

        // SecurityContext
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("current_user");
    }

    // ----------------- Helper Stubs -----------------

    private void mockCurrentUser() {
        when(userRepository.findByUsername("current_user"))
                .thenReturn(Optional.of(currentUser));
    }

    private void mockFetchPost(long id) {
        when(userUtils.fetchPost(id)).thenReturn(post);
    }

    // ---------------------- TESTS ----------------------

    @Test
    void createComment_Success() {
        mockCurrentUser();
        mockFetchPost(1L);

        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<CommentResponse> response =
                commentService.createComment(1L, "Test comment");

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Test comment", response.getBody().comment());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void createComment_ThrowsException_WhenPostNotFound() {
        mockCurrentUser();

        when(userUtils.fetchPost(2L))
                .thenThrow(new PostNotFoundException(POST_NOT_FOUND.getMessage()));

        PostNotFoundException ex = assertThrows(PostNotFoundException.class,
                () -> commentService.createComment(2L, "Test comment"));

        assertEquals(POST_NOT_FOUND.getMessage(), ex.getMessage());
        verify(commentRepository, never()).save(any());
    }

    @Test
    void deleteComment_Success() {
        mockCurrentUser();

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        ResponseEntity<String> response =
                commentService.deleteComment(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(DELETED_COMMENT_SUCCESSFULLY.getMessage(), response.getBody());
        verify(commentRepository).deleteById(1L);
    }

    @Test
    void deleteComment_ThrowsException_WhenCommentNotFound() {
        mockCurrentUser();
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        CommentNotFoundException ex = assertThrows(
                CommentNotFoundException.class,
                () -> commentService.deleteComment(1L)
        );

        assertEquals(COMMENT_NOT_FOUND.getMessage(), ex.getMessage());
        verify(commentRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteComment_ThrowsException_WhenNotCommentOwner() {
        mockCurrentUser();

        User otherUser = new User();
        otherUser.setId(99L);
        comment.setCommentedUser(otherUser);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        AccessDeniedException ex = assertThrows(
                AccessDeniedException.class,
                () -> commentService.deleteComment(1L)
        );

        assertEquals(UNAUTHORIZED.getMessage(), ex.getMessage());
        verify(commentRepository, never()).deleteById(anyLong());
    }

    @Test
    void getAllComments_Success() {
        Comment c1 = new Comment();
        c1.setContent("Comment 1");
        c1.setCommentedPost(post);
        c1.setCommentedUser(currentUser);

        Comment c2 = new Comment();
        c2.setContent("Comment 2");
        c2.setCommentedPost(post);
        c2.setCommentedUser(currentUser);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(commentRepository.findByCommentedPostId(1L))
                .thenReturn(Arrays.asList(c1, c2));

        ResponseEntity<List<CommentResponse>> response =
                commentService.getAllComments(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void getAllComments_ThrowsException_WhenPostNotFound() {
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        PostNotFoundException ex =
                assertThrows(PostNotFoundException.class,
                        () -> commentService.getAllComments(1L));

        assertEquals(POST_NOT_FOUND.getMessage(), ex.getMessage());
    }

    @Test
    void getAllComments_ReturnsEmpty_WhenNoComments() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(commentRepository.findByCommentedPostId(1L))
                .thenReturn(Collections.emptyList());

        ResponseEntity<List<CommentResponse>> response =
                commentService.getAllComments(1L);

        assertTrue(response.getBody().isEmpty());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
