package com.practice.routing;

import com.practice.HttpRequest;
import com.practice.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RouterTest {

    private Router router;

    @BeforeEach
    void setUp() {
        router = new Router();
    }

    // -- GET routes --

    @Test
    void testGetRouteMatchesAndReturnsResponse() {
        router.get("/hello", req -> {
            var res = new HttpResponse();
            res.setStatus(200, "OK");
            res.setBody("Hello!");
            return res;
        });

        HttpRequest request = buildRequest("GET", "/hello");
        HttpResponse response = router.dispatch(request);

        assertThat(response.getBody()).isEqualTo("Hello!");
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getStatusText()).isEqualTo("OK");
    }

    @Test
    void testGetRouteDoesNotMatchPost() {
        router.get("/hello", req -> {
            var res = new HttpResponse();
            res.setStatus(200, "OK");
            return res;
        });

        HttpRequest request = buildRequest("POST", "/hello");
        HttpResponse response = router.dispatch(request);

        assertThat(response.getStatusCode()).isEqualTo(404);
        assertThat(response.getStatusText()).isEqualTo("Not Found");
        assertThat(response.getBody()).isEqualTo("404 Not Found");
    }

    // -- POST routes --

    @Test
    void testPostRouteMatches() {
        router.post("/users", req -> {
            var res = new HttpResponse();
            res.setStatus(201, "Created");
            res.setBody("User created");
            return res;
        });

        HttpRequest request = buildRequest("POST", "/users");
        HttpResponse response = router.dispatch(request);

        assertThat(response.getStatusCode()).isEqualTo(201);
        assertThat(response.getStatusText()).isEqualTo("Created");
        assertThat(response.getBody()).contains("User created");
    }

    // -- PUT routes --

    @Test
    void testPutRouteMatches() {
        router.put("/users/:id", req -> {
            var res = new HttpResponse();
            res.setStatus(200, "OK");
            res.setBody("Updated " + req.getParam("id"));
            return res;
        });

        HttpRequest request = buildRequest("PUT", "/users/42");
        HttpResponse response = router.dispatch(request);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getStatusText()).isEqualTo("OK");
        assertThat(response.getBody()).contains("Updated " + request.getParam("id"));

    }

    // -- DELETE routes --

    @Test
    void testDeleteRouteMatches() {
        router.delete("/users/:id", req -> {
            var res = new HttpResponse();
            res.setStatus(200, "OK");
            res.setBody("Deleted " + req.getParam("id"));
            return res;
        });

        HttpRequest request = buildRequest("DELETE", "/users/7");
        HttpResponse response = router.dispatch(request);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getStatusText()).isEqualTo("OK");
        assertThat(response.getBody()).contains("Deleted " + request.getParam("id"));
    }

    // -- 404 for unregistered routes --

    @Test
    void testUnregisteredPathReturns404() {
        router.get("/hello", req -> {
            var res = new HttpResponse();
            res.setStatus(200, "OK");
            return res;
        });

        HttpRequest request = buildRequest("GET", "/unknown");
        HttpResponse response = router.dispatch(request);

        assertThat(response.getStatusCode()).isEqualTo(404);
        assertThat(response.getStatusText()).isEqualTo("Not Found");
        assertThat(response.getBody()).isEqualTo("404 Not Found");
    }

    @Test
    void testEmptyRouterReturns404() {
        HttpRequest request = buildRequest("GET", "/anything");
        HttpResponse response = router.dispatch(request);

        assertThat(response.getStatusCode()).isEqualTo(404);
        assertThat(response.getStatusText()).isEqualTo("Not Found");
        assertThat(response.getBody()).isEqualTo("404 Not Found");
    }

    // -- Path parameters --

    @Test
    void testSinglePathParam() {
        router.get("/users/:id", req -> {
            var res = new HttpResponse();
            res.setStatus(200, "OK");
            res.setBody("User " + req.getParam("id"));
            return res;
        });

        HttpRequest request = buildRequest("GET", "/users/123");
        HttpResponse response = router.dispatch(request);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getStatusText()).isEqualTo("OK");
        assertThat(response.getBody()).isEqualTo("User 123");
    }

    @Test
    void testMultiplePathParams() {
        router.get("/users/:userId/posts/:postId", req -> {
            var res = new HttpResponse();
            res.setStatus(200, "OK");
            res.setBody(req.getParam("userId") + "-" + req.getParam("postId"));
            return res;
        });

        HttpRequest request = buildRequest("GET", "/users/5/posts/99");
        HttpResponse response = router.dispatch(request);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getStatusText()).isEqualTo("OK");
        assertThat(response.getBody()).isEqualTo("5-99");
    }

    // -- Route priority (first match wins) --

    @Test
    void testFirstMatchingRouteWins() {
        router.get("/hello", req -> {
            var res = new HttpResponse();
            res.setStatus(200, "OK");
            res.setBody("first");
            return res;
        });

        router.get("/hello", req -> {
            var res = new HttpResponse();
            res.setStatus(200, "OK");
            res.setBody("second");
            return res;
        });

        HttpRequest request = buildRequest("GET", "/hello");
        HttpResponse response = router.dispatch(request);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getStatusText()).isEqualTo("OK");
        assertThat(response.getBody()).isEqualTo("first");
    }

    // -- Root path --

    @Test
    void testRootPath() {
        router.get("/", req -> {
            var res = new HttpResponse();
            res.setStatus(200, "OK");
            res.setBody("root");
            return res;
        });

        HttpRequest request = buildRequest("GET", "/");
        HttpResponse response = router.dispatch(request);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getStatusText()).isEqualTo("OK");
        assertThat(response.getBody()).isEqualTo("root");
    }

    // -- Multiple methods on same path --

    @Test
    void testDifferentMethodsSamePath() {
        router.get("/resource", req -> {
            var res = new HttpResponse();
            res.setStatus(200, "OK");
            res.setBody("GET response");
            return res;
        });

        router.post("/resource", req -> {
            var res = new HttpResponse();
            res.setStatus(201, "Created");
            res.setBody("POST response");
            return res;
        });

        HttpRequest getReq = buildRequest("GET", "/resource");
        HttpRequest postReq = buildRequest("POST", "/resource");

        HttpResponse getResp = router.dispatch(getReq);
        HttpResponse postResp = router.dispatch(postReq);

        assertThat(getResp.getStatusCode()).isEqualTo(200);
        assertThat(getResp.getStatusText()).isEqualTo("OK");
        assertThat(getResp.getBody()).isEqualTo("GET response");

        assertThat(postResp.getStatusCode()).isEqualTo(201);
        assertThat(postResp.getStatusText()).isEqualTo("Created");
        assertThat(postResp.getBody()).isEqualTo("POST response");
    }

    // -- Path param doesn't match extra segments --

    @Test
    void testPathDoesNotMatchExtraSegments() {
        router.get("/users/:id", req -> {
            var res = new HttpResponse();
            res.setStatus(200, "OK");
            return res;
        });

        HttpRequest request = buildRequest("GET", "/users/1/extra");
        HttpResponse response = router.dispatch(request);

        assertThat(response.getStatusCode()).isEqualTo(404);
        assertThat(response.getStatusText()).isEqualTo("Not Found");
        assertThat(response.getBody()).isEqualTo("404 Not Found");
    }

    @Test
    void testPathDoesNotMatchFewerSegments() {
        router.get("/users/:id/posts", req -> {
            var res = new HttpResponse();
            res.setStatus(200, "OK");
            return res;
        });

        HttpRequest request = buildRequest("GET", "/users/1");
        HttpResponse response = router.dispatch(request);

        assertThat(response.getStatusCode()).isEqualTo(404);
        assertThat(response.getStatusText()).isEqualTo("Not Found");
        assertThat(response.getBody()).isEqualTo("404 Not Found");
    }

    // -- Helpers --

    private HttpRequest buildRequest(String method, String path) {
        return new HttpRequest.HttpRequestBuilder()
                .setMethod(method)
                .setPath(path)
                .setVersion("HTTP/1.1")
                .build();
    }
}
