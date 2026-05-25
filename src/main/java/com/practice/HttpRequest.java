package com.practice;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class HttpRequest {
    private final String method;
    private final String path;
    private final String version;
    private final Map<String, String> headers;
    private final Map<String, String> params;
    private final byte[] body;
    
    private HttpRequest(HttpRequestBuilder builder) {
        this.method = builder.method;
        this.path = builder.path;
        this.version = builder.version;
        this.headers = Map.copyOf(builder.headers);
        this.body = builder.body;
        this.params = new HashMap<>();
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
    
    public void addPathParam(String key, String value) {
        this.params.put(key, value);
    }
    
    public String getParam(String key) { 
        return this.params.get(key);
    }
    
    public String getBodyAsString() {
        if (body == null) {
            return "";
        }
        return new String(this.body, StandardCharsets.UTF_8);
    }
    
    public byte[] getBody() {
        return this.body;
    }
    
    public static class HttpRequestBuilder {
        private String method;
        private String path;
        private String version;
        private final Map<String, String> headers = new HashMap<>();
        private List<byte[]> bodyChunks;
        private byte[] body;
        
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
        
        public HttpRequestBuilder appendBody(byte[] chunk) {
            if (this.bodyChunks == null) {
                this.bodyChunks = new ArrayList<>();
            }
            this.bodyChunks.add(chunk);
            return this;
        }
        
        public HttpRequestBuilder addHeader(String key, String value) {
            Objects.requireNonNull(key, "header key shouldn't be null");
            Objects.requireNonNull(value, "header value shouldn't be null");
            headers.put(key.toLowerCase(), value);
            return this;
        }
        
        public HttpRequest build() {
            if (bodyChunks != null && !bodyChunks.isEmpty()) {
                this.body = buildBody(bodyChunks);
            }
            return new HttpRequest(this);
        }
        
        private byte[] buildBody(List<byte[]> chunks) {
            int len = bodyChunks.stream().mapToInt(chunk -> chunk.length).sum();
            byte[] body = new byte[len];
            int offset = 0;

            for (byte[] chunk : bodyChunks) {
                System.arraycopy(chunk, 0, body, offset, chunk.length);
                offset += chunk.length;
            }
            return body;
        }
    }
}
    