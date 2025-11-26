package com.projects.My_Instagram.helper;

import com.projects.My_Instagram.DTOs.response.UserResponse;
import com.projects.My_Instagram.models.User;

public class UserHelper {
    public static UserResponse formUserResponse(User createdUser) {
        UserResponse userResponse = new UserResponse();
        userResponse.setId(createdUser.getId());
        userResponse.setFullName(createdUser.getFullName());
        userResponse.setProfilePicUrl(createdUser.getProfilePicUrl());
        userResponse.setUsername(createdUser.getUsername());

        return userResponse;
    }
}
