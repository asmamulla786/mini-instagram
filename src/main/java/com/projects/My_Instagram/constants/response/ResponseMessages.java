package com.projects.My_Instagram.constants.response;

public enum ResponseMessages {
    USER_CREATED_SUCCESSFULLY("User Created Successfully");
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
