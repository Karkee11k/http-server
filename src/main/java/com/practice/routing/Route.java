package com.practice.routing;

import java.util.*;
import java.util.regex.Pattern;

public class Route { 
    private final String method;
    private final String pattern;
    private final Router.HttpHandler handler;
    private final Pattern regex;
    private final List<String> paramNames;
    
    public Route(String method, String pattern, Router.HttpHandler handler) {
        this.method = method;
        this.pattern = pattern;
        this.handler = handler;
        this.paramNames = new ArrayList<>();
        this.regex = buildRegex(pattern);
    }
    
    // converts "/user/:id" to regex "/users/([^/]+)"
    private Pattern buildRegex(String pattern) {
        var builder = new StringBuilder();
        for (var split : pattern.split("/")) {
            if (split.isEmpty()) {
                continue;
            }
            if (split.startsWith(":"))  {
                paramNames.add(split.substring(1));
                builder.append("/([^/]+)");
            } else {
                builder.append("/").append(Pattern.quote(split));
            }
        }
        
        // handle root path
        var regexStr = builder.isEmpty() ? "/" : builder.toString();
        return Pattern.compile(regexStr);
    }
    
    public RouteMatch matches(String method, String path) {
        if (!this.method.equalsIgnoreCase(method)) {
            return RouteMatch.noMatch();
        }
        
        var matcher = this.regex.matcher(path);
        if (!matcher.matches()) {
            return RouteMatch.noMatch();
        }
        
        var params = new HashMap<String, String>();
        for (int i = 0; i < paramNames.size(); ++i) {
            params.put(paramNames.get(i), matcher.group(i + 1));
        }
        return RouteMatch.match(params);
    }
    
    public String method() {
        return method;
    }
    
    public String pattern() {
        return pattern;
    }
    
    public Router.HttpHandler handler() {
        return handler;
    }
    
    public static class RouteMatch {
        private final boolean matches;
        private final Map<String, String> params;
        
        private RouteMatch(boolean matches, Map<String, String> params) {
            this.matches = matches;
            this.params = Map.copyOf(params);
        }
        
        public static RouteMatch noMatch() {
            return new RouteMatch(false, Collections.emptyMap());
        }
        
        public static RouteMatch match(Map<String, String> params) { 
            return new RouteMatch(true, params);
        }
        
        public Map<String, String> params() {
            return this.params;
        }
        
        public boolean matches() {
            return this.matches;
        }
    }
}

