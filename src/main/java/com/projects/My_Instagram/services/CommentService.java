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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.projects.My_Instagram.constants.exception.ExceptionMessages.*;
import static com.projects.My_Instagram.constants.response.ResponseMessages.*;
import static com.projects.My_Instagram.helper.Helper.*;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final UserUtils userUtils;

    public CommentService(CommentRepository commentRepository, UserRepository userRepository, PostRepository postRepository, UserUtils userUtils) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.userUtils = userUtils;
    }

    public ResponseEntity<CommentResponse> createComment(Long postId, String content) {
        User currentUser = fetchCurrentUser();
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setUploadedAt(new Date());
        comment.setCommentedUser(currentUser);
        Post commentedPost = userUtils.fetchPost(postId);
        comment.setCommentedPost(commentedPost);
        Comment savedComment = commentRepository.save(comment);
        return ResponseEntity.status(HttpStatus.CREATED).body(formCommentResponse(savedComment));
    }

    private User fetchCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> user = userRepository.findByUsername(username);
        return user.get();
    }

    public ResponseEntity<String> deleteComment(Long commentId) {
        Comment comment = fetchComment(commentId);
        User currentUser = fetchCurrentUser();

        if(!Objects.equals(comment.getCommentedUser().getUsername(), currentUser.getUsername())){
            throw new AccessDeniedException(UNAUTHORIZED.getMessage());
        }

        commentRepository.deleteById(commentId);
        return ResponseEntity.status(HttpStatus.OK).body(DELETED_COMMENT_SUCCESSFULLY.getMessage());
    }

    private Comment fetchComment(Long commentId) {
        Optional<Comment> comment = commentRepository.findById(commentId);

        if(comment.isEmpty()){
            throw new CommentNotFoundException(COMMENT_NOT_FOUND.getMessage());
        }

        return comment.get();
    }

    public ResponseEntity<List<CommentResponse>> getAllComments(Long postId) {
        List<CommentResponse> comments = new ArrayList<>();

        if(postRepository.findById(postId).isEmpty()){
            throw new PostNotFoundException(POST_NOT_FOUND.getMessage());
        }

        for (Comment comment : commentRepository.findByCommentedPostId(postId)) {
            comments.add(formCommentResponse(comment));
        }

        return ResponseEntity.status(HttpStatus.OK).body(comments);
    }
}
