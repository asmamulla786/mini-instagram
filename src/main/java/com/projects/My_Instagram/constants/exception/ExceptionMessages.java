package com.projects.My_Instagram.constants.exception;

public enum ExceptionMessages {
    USER_NOT_FOUND("User not found"),
    USER_NAME_EXISTS("Username already taken"),
    POST_NOT_FOUND("Post not found"),
    USER_NAME_NULL("User Should Not be null"),
    PASS_WORD_NUll("Password is mandatory"),
    INVALID_CREDENTIALS("Invalid username or password");

    private final String message;

    ExceptionMessages(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
