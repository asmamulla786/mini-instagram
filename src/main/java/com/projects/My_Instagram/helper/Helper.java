package com.projects.My_Instagram.helper;

import com.projects.My_Instagram.DTOs.response.PostResponse;
import com.projects.My_Instagram.DTOs.response.UserResponse;
import com.projects.My_Instagram.models.Post;
import com.projects.My_Instagram.models.User;

public class Helper {
    public static UserResponse formUserResponse(User createdUser) {
        UserResponse userResponse = new UserResponse();
        userResponse.setId(createdUser.getId());
        userResponse.setFullName(createdUser.getFullName());
        userResponse.setProfilePicUrl(createdUser.getProfilePicUrl());
        userResponse.setUsername(createdUser.getUsername());

        return userResponse;
    }

    public static PostResponse formPostResponse(Post post){
        PostResponse postResponse = new PostResponse();
        postResponse.setUser(formUserResponse(post.getUser()));
        postResponse.setCaption(post.getCaption());
        postResponse.setImageUrl(post.getImageUrl());
        postResponse.setUploadedAt(post.getUploadedAt());
        postResponse.setId(post.getId());
        return postResponse;
    }
}
