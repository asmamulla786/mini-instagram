package com.projects.My_Instagram.Constants;

public enum ExceptionMessages {
    USER_NOT_FOUND("User not found"),
    USER_NAME_EXISTS("Username already taken"),
    POST_NOT_FOUND("Post not found"),
    USER_NAME_NULL("User Should Not be null");

    private final String message;

    ExceptionMessages(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
