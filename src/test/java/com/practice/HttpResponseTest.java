package com.practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HttpResponseTest {
    
    @Test
    void testSerialization() {
        String raw =
                "HTTP/1.1 200 OK\r\n" +
                "Content-Type: application/json\r\n" +
                "\r\n";

        HttpResponse response = new HttpResponse();
        response.setStatus(200, "OK");
        response.setHeader("Content-Type", "application/json");

        assertThat(response.toBytes()).isEqualTo(raw.getBytes());
    }

    @Test
    void testResponseWithBody() {
        String body = "{\"name\":\"karthi\"}";
        String raw =
                "HTTP/1.1 200 OK\r\n" +
                "Content-Length: " + body.getBytes().length + "\r\n" +
                "\r\n" +
                body;

        HttpResponse response = new HttpResponse();
        response.setStatus(200, "OK");
        response.setBody(body);

        assertThat(new String(response.toBytes())).isEqualTo(raw);
    }
}
