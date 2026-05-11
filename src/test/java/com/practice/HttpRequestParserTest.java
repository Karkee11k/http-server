package com.practice;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class HttpRequestParserTest {

    @Test
    void testSimpleGetRequest() throws IOException {
        String raw =
                "GET /hello HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n";

        HttpRequest request = parse(raw);

        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getPath()).isEqualTo("/hello");
        assertThat(request.getVersion()).isEqualTo("HTTP/1.1");
        assertThat(request.getHeader("host")).isEqualTo("localhost");
    }

    @Test
    void testPostWithBody() throws IOException {
        String body = "{\"name\":\"karthi\"}";
        String raw =
                "POST /users HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Content-Length: " + body.length() + "\r\n" +
                "\r\n" +
                body;

        HttpRequest request = parse(raw);

        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/users");
        assertThat(request.getRequestBody()).isEqualTo(body);
    }

    @Test
    void testPutWithBody() throws IOException {
        String body = "{\"name\":\"updated\"}";
        String raw =
                "PUT /users/1 HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Content-Length: " + body.length() + "\r\n" +
                "\r\n" +
                body;

        HttpRequest request = parse(raw);

        assertThat(request.getMethod()).isEqualTo("PUT");
        assertThat(request.getPath()).isEqualTo("/users/1");
        assertThat(request.getRequestBody()).isEqualTo(body);
    }

    @Test
    void testPatchWithBody() throws IOException {
        String body = "{\"name\":\"patched\"}";
        String raw =
                "PATCH /users/1 HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Content-Length: " + body.length() + "\r\n" +
                "\r\n" +
                body;

        HttpRequest request = parse(raw);

        assertThat(request.getMethod()).isEqualTo("PATCH");
        assertThat(request.getRequestBody()).isEqualTo(body);
    }

    @Test
    void testGetRequestHasNoBody() throws IOException {
        String raw =
                "GET /hello HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n";

        HttpRequest request = parse(raw);

        assertThat(request.getRequestBody()).isNull();
    }

    @Test
    void testDeleteRequestHasNoBody() throws IOException {
        String raw =
                "DELETE /users/1 HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n";

        HttpRequest request = parse(raw);

        assertThat(request.getMethod()).isEqualTo("DELETE");
        assertThat(request.getPath()).isEqualTo("/users/1");
        assertThat(request.getRequestBody()).isNull();
    }

    @Test
    void testMultipleHeaders() throws IOException {
        String raw =
                "GET /hello HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Accept: application/json\r\n" +
                "Authorization: Bearer token123\r\n" +
                "Content-Type: text/plain\r\n" +
                "\r\n";

        HttpRequest request = parse(raw);

        assertThat(request.getHeader("host")).isEqualTo("localhost");
        assertThat(request.getHeader("accept")).isEqualTo("application/json");
        assertThat(request.getHeader("authorization")).isEqualTo("Bearer token123");
        assertThat(request.getHeader("content-type")).isEqualTo("text/plain");
    }

    @Test
    void testHeadersAreCaseInsensitive() throws IOException {
        String raw =
                "GET / HTTP/1.1\r\n" +
                "Content-Type: text/html\r\n" +
                "\r\n";

        HttpRequest request = parse(raw);

        assertThat(request.getHeader("Content-Type")).isEqualTo("text/html");
        assertThat(request.getHeader("content-type")).isEqualTo("text/html");
        assertThat(request.getHeader("CONTENT-TYPE")).isEqualTo("text/html");
    }

    @Test
    void testHeaderValueWithColon() throws IOException {
        String raw =
                "GET / HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "\r\n";

        HttpRequest request = parse(raw);

        assertThat(request.getHeader("host")).isEqualTo("localhost:8080");
    }

    @Test
    void testEmptyRequestThrows() {
        InputStream in = new ByteArrayInputStream(new byte[0]);

        assertThatThrownBy(() -> HttpRequestParser.parse(in))
                .isInstanceOf(IOException.class)
                .hasMessage("Empty request");
    }

    @Test
    void testInvalidRequestLineThrows() {
        String raw = "INVALID\r\n\r\n";
        InputStream in = new ByteArrayInputStream(raw.getBytes());

        assertThatThrownBy(() -> HttpRequestParser.parse(in))
                .isInstanceOf(IOException.class)
                .hasMessage("Invalid request line");
    }

    @Test
    void testPostWithoutContentLengthHasNoBody() throws IOException {
        String raw =
                "POST /users HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n";

        HttpRequest request = parse(raw);

        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getRequestBody()).isNull();
    }

    @Test
    void testRequestWithQueryPath() throws IOException {
        String raw =
                "GET /search?q=java&page=1 HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n";

        HttpRequest request = parse(raw);

        assertThat(request.getPath()).isEqualTo("/search?q=java&page=1");
    }

    private HttpRequest parse(String raw) throws IOException {
        InputStream in = new ByteArrayInputStream(raw.getBytes());
        return HttpRequestParser.parse(in);
    }
}
