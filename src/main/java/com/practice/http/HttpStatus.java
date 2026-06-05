package com.practice.http;

public enum HttpStatus {
    OK(200, "OK"),
    CREATED(201, "Created"),
    
    BAD_REQUEST(400, "Bad Request"),
    NOT_FOUND(404, "Not Found"),
    PAYLOAD_TOO_LARGE(413, "Payload Too Large"),
    
    INTERNAL_SERVER_ERROR(500, "Internal Server Error");
    
    private final int code;
    private final String text;
    
    HttpStatus(int code, String text) {
        this.code = code;
        this.text = text;
    }
    
    public int code() {
        return code;
    }
    
    public String text() {
        return text;
    }
}
