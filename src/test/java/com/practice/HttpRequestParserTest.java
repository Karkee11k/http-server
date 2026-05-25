package com.practice;

import com.practice.exceptions.BadRequestException;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class HttpRequestParserTest {

    @Test
    void testSimpleGetRequest() throws IOException, BadRequestException {
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
    void testPostWithBody() throws IOException, BadRequestException {
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
        assertThat(request.getBodyAsString()).isEqualTo(body);
    }

    @Test
    void testPutWithBody() throws IOException, BadRequestException {
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
        assertThat(request.getBodyAsString()).isEqualTo(body);
    }

    @Test
    void testPatchWithBody() throws IOException, BadRequestException {
        String body = "{\"name\":\"patched\"}";
        String raw =
                "PATCH /users/1 HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Content-Length: " + body.length() + "\r\n" +
                "\r\n" +
                body;

        HttpRequest request = parse(raw);

        assertThat(request.getMethod()).isEqualTo("PATCH");
        assertThat(request.getBodyAsString()).isEqualTo(body);
    }

    @Test
    void testGetRequestHasNoBody() throws IOException, BadRequestException {
        String raw =
                "GET /hello HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n";

        HttpRequest request = parse(raw);

        assertThat(request.getBody()).isNull();
    }

    @Test
    void testDeleteRequestHasNoBody() throws IOException, BadRequestException {
        String raw =
                "DELETE /users/1 HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n";

        HttpRequest request = parse(raw);

        assertThat(request.getMethod()).isEqualTo("DELETE");
        assertThat(request.getPath()).isEqualTo("/users/1");
        assertThat(request.getBody()).isNull();
    }

    @Test
    void testMultipleHeaders() throws IOException, BadRequestException {
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
    void testHeadersAreCaseInsensitive() throws IOException, BadRequestException {
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
    void testHeaderValueWithColon() throws IOException, BadRequestException {
        String raw =
                "GET / HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "\r\n";

        HttpRequest request = parse(raw);

        assertThat(request.getHeader("host")).isEqualTo("localhost:8080");
    }

    @Test
    void testEmptyRequestThrows() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(new byte[0])));

        assertThatThrownBy(() -> HttpRequestParser.parse(reader))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Empty request line");
    }

    @Test
    void testInvalidRequestLineThrows() {
        String raw = "INVALID\r\n\r\n";
        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(raw.getBytes())));

        assertThatThrownBy(() -> HttpRequestParser.parse(reader))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid request line");
    }

    @Test
    void testPostWithoutContentLengthHasNoBody() throws IOException, BadRequestException {
        String raw =
                "POST /users HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n";

        HttpRequest request = parse(raw);

        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getBody()).isNull();
    }

    @Test
    void testRequestWithQueryPath() throws IOException, BadRequestException {
        String raw =
                "GET /search?q=java&page=1 HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n";

        HttpRequest request = parse(raw);

        assertThat(request.getPath()).isEqualTo("/search?q=java&page=1");
    }

    private HttpRequest parse(String raw) throws IOException, BadRequestException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(raw.getBytes())));
        return HttpRequestParser.parse(reader);
    }
}
