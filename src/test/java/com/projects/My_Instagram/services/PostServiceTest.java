//package com.projects.My_Instagram.services;
//
//import com.projects.My_Instagram.DTOs.request.PostRequest;
//import com.projects.My_Instagram.exceptions.PostNotFoundException;
//import com.projects.My_Instagram.exceptions.UserNotFoundException;
//import com.projects.My_Instagram.models.Post;
//import com.projects.My_Instagram.models.User;
//import com.projects.My_Instagram.repositories.PostRepository;
//import com.projects.My_Instagram.repositories.UserRepository;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.List;
//import java.util.Optional;
//
//import static com.projects.My_Instagram.constants.exception.ExceptionMessages.*;
//
//@ExtendWith(MockitoExtension.class)
//class PostServiceTest {
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private
//    PostRepository postRepository;
//
//    @InjectMocks
//    private PostService postService;
//
//    @Test
//    void testCreatePost_Success() {
//        PostRequest request = new PostRequest();
//        request.setUsername("asma_123");
//        request.setCaption("Hello!");
//        request.setImageUrl("http://image.jpg");
//
//        User user = new User();
//        user.setUsername("asma_123");
//
//        Mockito.when(userRepository.findByUsername("asma_123")).thenReturn(Optional.of(user));
//
//        Post savedPost = new Post();
//        savedPost.setCaption("Hello!");
//        savedPost.setImageUrl("http://image.jpg");
//        savedPost.setUser(user);
//
//        Mockito.when(postRepository.save(Mockito.any(Post.class))).thenReturn(savedPost);
//
//        Post post = postService.createPost(request);
//
//        Assertions.assertEquals("Hello!", post.getCaption());
//        Assertions.assertEquals("http://image.jpg", post.getImageUrl());
//        Assertions.assertEquals("asma_123", post.getUser().getUsername());
//
//        Mockito.verify(postRepository, Mockito.times(1))
//                .save(Mockito.any(Post.class));
//    }
//
//    @Test
//    void testCreatePost_UserNotFound() {
//        PostRequest request = new PostRequest();
//        request.setUsername("unknown_user");
//        request.setCaption("Hi");
//        request.setImageUrl("url");
//
//        Mockito.when(userRepository.findByUsername("unknown_user"))
//                .thenThrow(new UserNotFoundException(USER_NOT_FOUND.getMessage()));
//
//        UserNotFoundException exception =
//                Assertions.assertThrows(UserNotFoundException.class, () ->
//                        postService.createPost(request)
//                );
//
//        Assertions.assertEquals(USER_NOT_FOUND.getMessage(), exception.getMessage());
//    }
//    @Test
//    void testDeletePost_Success() {
//        Long postId = 1L;
//
//        User user = new User();
//        user.setUsername("asma");
//
//        Post post = new Post();
//        post.setId(postId);
//        post.setUser(user);
//
//        Mockito.when(postRepository.findById(postId))
//                .thenReturn(Optional.of(post));
//
//        postService.deletePost(postId);
//
//        Mockito.verify(postRepository, Mockito.times(1))
//                .deleteById(postId);
//    }
//
//    @Test
//    void testDeletePost_PostNotFound() {
//        Long postId = 1L;
//
//        Mockito.when(postRepository.findById(postId))
//                .thenReturn(Optional.empty());
//
//        PostNotFoundException exception =
//                Assertions.assertThrows(PostNotFoundException.class, () ->
//                        postService.deletePost(postId)
//                );
//
//        Assertions.assertEquals(POST_NOT_FOUND.getMessage(), exception.getMessage());
//    }
//
//
//    @Test
//    void testGetAllPostsOfUser_Success() {
//        String username = "asma";
//
//        User user = new User();
//        user.setUsername(username);
//
//        Post post1 = new Post();
//        post1.setId(1L);
//        post1.setUser(user);
//
//        Post post2 = new Post();
//        post2.setId(2L);
//        post2.setUser(user);
//
//        List<Post> posts = List.of(post1, post2);
//
//        Mockito.when(userRepository.findByUsername(username))
//                .thenReturn(Optional.of(user));
//
//        Mockito.when(postRepository.findByUser(user))
//                .thenReturn(posts);
//
//        List<Post> allPosts = postService.getAllPostOfUser(username);
//
//        Assertions.assertEquals(2, allPosts.size());
//        Assertions.assertEquals(1L, allPosts.get(0).getId());
//        Assertions.assertEquals(2L, allPosts.get(1).getId());
//
//        Mockito.verify(userRepository, Mockito.times(1))
//                .findByUsername(username);
//
//        Mockito.verify(postRepository, Mockito.times(1))
//                .findByUser(user);
//    }
//
//
//    @Test
//    void testDeleteAllPostsOfUser_Success() {
//        String username = "asma";
//
//        User user = new User();
//        user.setUsername(username);
//
//        Mockito.when(userRepository.findByUsername(username))
//                .thenReturn(Optional.of(user));
//
//        postService.deleteAllPostOfUser(username);
//
//        Mockito.verify(userRepository, Mockito.times(1))
//                .findByUsername(username);
//
//        Mockito.verify(postRepository, Mockito.times(1))
//                .deleteByUser(user);
//    }
//
//}