package com.projects.My_Instagram.DTOs.response;

import java.util.Date;

public record CommentResponse(String username, Date uploadedAt, String comment) {
}
