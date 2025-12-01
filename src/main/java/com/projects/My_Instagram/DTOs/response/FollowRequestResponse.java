package com.projects.My_Instagram.DTOs.response;

import java.util.Date;

public class FollowRequestResponse {
    private String username;
    private Date requestedAt;

    public FollowRequestResponse(String username, Date requestedAt) {
        this.username = username;
        this.requestedAt = requestedAt;
    }

    public String getUsername() {
        return username;
    }

    public Date getRequestedAt() {
        return requestedAt;
    }
}
