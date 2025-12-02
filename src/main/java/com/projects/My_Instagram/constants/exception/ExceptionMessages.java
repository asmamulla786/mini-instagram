package com.projects.My_Instagram.constants.exception;

public enum ExceptionMessages {
    USER_NOT_FOUND("User not found"),
    USER_NAME_EXISTS("Username already taken"),
    POST_NOT_FOUND("Post not found"),
    USER_NAME_NULL("User Should Not be null"),
    PASS_WORD_NUll("Password is mandatory"),
    UNAUTHORIZED("Access denied: You do not have permission to delete this."),
    COMMENT_NOT_FOUND("Comment not found"),

    //Like / Unlike

    YOU_CANNOT_LIKE("You cannot like or comment on this private user's post"),
    //Follow / Unfollow

    FOLLOW_SELF("You can't follow yourself"),
    ALREADY_FOLLOWING("You are already following the user"),
    ALREADY_REQUESTED("You already requested"),
    NOT_FOLLOWING("You are not following the user"),

    // Follow Request

    FOLLOW_REQUEST_NOT_FOUND("You did not get the follow request from that user");

    private final String message;

    ExceptionMessages(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
