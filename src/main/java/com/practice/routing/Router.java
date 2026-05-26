package com.practice.routing;

import com.practice.HttpRequest;
import com.practice.HttpResponse;
import com.practice.http.HttpMethods;
import com.practice.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

public class Router {
    private final List<Route> routes;
    
    public Router() {
        this.routes = new ArrayList<>();
    }
    
    public void get(String path, HttpHandler handler) {
        this.routes.add(new Route(HttpMethods.GET, path, handler));
    }

    public void post(String path, HttpHandler handler) {
        this.routes.add(new Route(HttpMethods.POST, path, handler));
    }
    
    public void put(String path, HttpHandler handler) {
        this.routes.add(new Route(HttpMethods.PUT, path, handler));
    }
    
    public void delete(String path, HttpHandler handler) {
        this.routes.add(new Route(HttpMethods.DELETE, path, handler));
    }
    
    public HttpResponse dispatch(HttpRequest request) {
        var method = request.getMethod();
        var path = request.getPath();
        
        for (var route : routes) {
            var match = route.matches(method, path);
            if (match.matches()) {
                match.params().forEach(request::addPathParam);
                return route.handler().handle(request);
            }
        }
        
        var response = new HttpResponse();
        response.setStatus(HttpStatus.NOT_FOUND);
        response.setHeader("Content-Type", "text/plain");
        response.setBody(HttpStatus.NOT_FOUND.text());
        return response;
    }
    
    @FunctionalInterface
    public interface HttpHandler {
        HttpResponse handle(HttpRequest request);
    }
}
