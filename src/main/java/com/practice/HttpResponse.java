package com.practice;

import com.practice.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class HttpResponse {
    private static final String CRLF = "\r\n";
    private static final String VERSION = "HTTP/1.1";
    
    private HttpStatus status;
    private final Map<String, String> headers;
    private String body;
    
    public HttpResponse() {
        this.headers = new LinkedHashMap<>();
    }
    
    public void setStatus(HttpStatus status) {
        this.status = status;
    }
    
    public void setBody(String body) {
        this.body = Objects.requireNonNull(body, "response body shouldn't be null");
    }
    
    public void setHeader(String key, String value) {
        Objects.requireNonNull(key, "header key shouldn't be null");
        Objects.requireNonNull(value, "header value shouldn't be null");
        headers.put(key, value);
    }
    
    public boolean shouldClose() {
        var connection = this.headers.get("Connection");
        return connection != null && connection.equalsIgnoreCase("close");
    }
    
    public HttpStatus getStatus() {
        return status;
    }
    
    public int getStatusCode() {
        return this.status.code();
    }
    
    public String getStatusText() {
        return this.status.text();
    }
    
    public String getBody() {
        return this.body;
    }
    
    public byte[] toBytes() {
        StringBuilder builder = new StringBuilder();
        builder.append(VERSION).append(" ")
                .append(status.code()).append(" ")
                .append(status.text()).append(CRLF);
        
        Map<String, String> outHeaders = new LinkedHashMap<>(this.headers);
        if (this.body != null && !body.isEmpty()) {
            outHeaders.put("Content-Length", String.valueOf(this.body.getBytes(StandardCharsets.UTF_8).length));
        }
        
        for (Map.Entry<String, String> entry : outHeaders.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            builder.append(key).append(": ").append(value).append(CRLF);
        }
        builder.append(CRLF);
        
        if (body != null && !body.isEmpty()) {
            builder.append(body);
        }
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }
    
    public static HttpResponse from(String raw) {
        var response = new HttpResponse();
        return response; 
    }
}
