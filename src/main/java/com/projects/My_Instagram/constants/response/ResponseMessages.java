package com.projects.My_Instagram.constants.response;

public enum ResponseMessages {
    USER_CREATED_SUCCESSFULLY("User Created Successfully"),
    LIKED_SUCCESSFULLY("Post liked successfully"),
    ALREADY_LIKED("Post liked successfully"),
    NOT_LIKED("You haven't liked the post"),
    UNLIKED_SUCCESSFULLY("Post unliked successfully"),
    DELETED_COMMENT_SUCCESSFULLY("Deleted comment successfully");
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
