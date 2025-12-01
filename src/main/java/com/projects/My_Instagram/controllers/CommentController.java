package com.projects.My_Instagram.controllers;

import com.projects.My_Instagram.DTOs.request.CreateCommentRequest;
import com.projects.My_Instagram.DTOs.response.CommentResponse;
import com.projects.My_Instagram.services.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/posts/{post_id}/comments")
    public ResponseEntity<CommentResponse> createComment(@PathVariable Long post_id, @RequestBody CreateCommentRequest commentRequest){
        return commentService.createComment(post_id, commentRequest.getContent());
    }

    @DeleteMapping("comments/{comment_id}")
    public ResponseEntity<String> deleteComment(@PathVariable Long comment_id){
        return commentService.deleteComment(comment_id);
    }

    @GetMapping("/posts/{post_id}/comments")
    public ResponseEntity<List<CommentResponse>> getAllCommentsOfPost(@PathVariable Long post_id){
        return commentService.getAllComments(post_id);
    }
}
