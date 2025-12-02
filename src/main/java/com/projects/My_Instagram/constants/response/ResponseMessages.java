package com.projects.My_Instagram.constants.response;

public enum ResponseMessages {
    // User / Auth
    USER_CREATED_SUCCESSFULLY("User Created Successfully"),

    // Post Likes
    LIKED_SUCCESSFULLY("Post liked successfully"),
    ALREADY_LIKED("You have already liked the post"),
    NOT_LIKED("You haven't liked the post"),
    UNLIKED_SUCCESSFULLY("Post unliked successfully"),

    // Comments
    DELETED_COMMENT_SUCCESSFULLY("Deleted comment successfully"),
    COMMENT_CREATED_SUCCESSFULLY("Comment added successfully"),

    // Follow / Unfollow
    FOLLOW_SUCCESS("User successfully added to your following list"),
    FOLLOW_REQUEST_SENT("Follow request sent successfully"),
    UNFOLLOW_SUCCESS("Unfollowed successfully"),

    // Follow Request Actions
    FOLLOW_REQUEST_ACCEPTED("Follow Request Accepted successfully"),
    FOLLOW_REQUEST_REJECTED("Follow Request Rejected successfully");

    private String message;

    ResponseMessages(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
