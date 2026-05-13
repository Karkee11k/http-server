package com.practice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class HttpResponse {
    private static final String CRLF = "\r\n";
    private static final String VERSION = "HTTP/1.1";
    
    private int statusCode;
    private String statusText;
    private final Map<String, String> headers;
    private String body;
    
    public HttpResponse() {
        this.headers = new LinkedHashMap<>();
    }
    
    public void setStatus(int code, String text) {
        this.statusCode = code;
        this.statusText = Objects.requireNonNull(text, "status text shouldn't be null");
    }
    
    public void setBody(String body) {
        this.body = Objects.requireNonNull(body, "response body shouldn't be null");
    }
    
    public void setHeader(String key, String value) {
        Objects.requireNonNull(key, "header key shouldn't be null");
        Objects.requireNonNull(value, "header value shouldn't be null");
        headers.put(key, value);
    }
    
    public byte[] toBytes() {
        StringBuilder builder = new StringBuilder();
        builder.append(VERSION).append(" ")
                .append(statusCode).append(" ")
                .append(statusText).append(CRLF);
        
        Map<String, String> outHeaders = new LinkedHashMap<>(this.headers);
        if (this.body != null && !body.isEmpty()) {
            outHeaders.put("Content-Length", String.valueOf(this.body.getBytes().length));
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
        
        return builder.toString().getBytes();
    }
}
