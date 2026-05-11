package com.practice;

import java.util.*;

public class HttpRequest {
    private final String method;
    private final String path;
    private final String version;
    private final Map<String, String> headers;
    private final String body;
    
    private HttpRequest(HttpRequestBuilder builder) {
        this.method = builder.method;
        this.path = builder.path;
        this.version = builder.version;
        this.headers = Map.copyOf(builder.headers);
        this.body = builder.body;
    }
    
    public String getMethod() {
        return this.method;
    }
    
    public String getPath() {
        return this.path;
    }
    
    public String getVersion() {
        return this.version;
    }
    
    public String getHeader(String header) {
        return this.headers.get(header.toLowerCase());
    }
    
    public String getRequestBody() {
        return this.body;
    }
    
    public static class HttpRequestBuilder {
        private String method;
        private String path;
        private String version;
        private final Map<String, String> headers = new HashMap<>();
        private String body;
        
        public HttpRequestBuilder setMethod(String method) {
            this.method = Objects.requireNonNull(method, "method can't be null");
            return this;
        }
        
        public HttpRequestBuilder setPath(String path) {
            this.path = Objects.requireNonNull(path, "path shouldn't be null");
            return this;
        }
        
        public HttpRequestBuilder setVersion(String version) {
            this.version = Objects.requireNonNull(version, "version shouldn't be null");
            return this;
        }

        public HttpRequestBuilder setBody(String body) {
            this.body = Objects.requireNonNull(body, "body shouldn't be null");
            return this;
        }
        
        public HttpRequestBuilder addHeader(String key, String value) {
            Objects.requireNonNull(key, "header key shouldn't be null");
            Objects.requireNonNull(value, "header value shouldn't be null");
            headers.put(key.toLowerCase(), value);
            return this;
        }
        
        public HttpRequest build() {
            return new HttpRequest(this);
        }
    }
}
    