package com.projects.My_Instagram.DTOs.request;

import jakarta.validation.constraints.NotBlank;

public class CreateCommentRequest {

    @NotBlank(message = "Content should not be empty")
    private String content;


    public String getContent() {
        return content;
    }
}
