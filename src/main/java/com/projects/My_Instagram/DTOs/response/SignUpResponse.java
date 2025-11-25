package com.projects.My_Instagram.DTOs.response;

public class SignUpResponse {
    private String message;
    private UserResponse user;

    public SignUpResponse(String message, UserResponse user) {
        this.message = message;
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public UserResponse getUser() {
        return user;
    }
}

