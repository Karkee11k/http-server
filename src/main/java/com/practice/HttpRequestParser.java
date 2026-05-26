package com.practice;

import com.practice.exceptions.BadRequestException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.practice.HttpRequest.HttpRequestBuilder;

public class HttpRequestParser {
    public static HttpRequest parse(BufferedReader reader) throws BadRequestException, IOException {
        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();

        String requestLine = reader.readLine();
        if (requestLine == null) throw new BadRequestException("Empty request line"); 

        String[] splits = requestLine.split(" ");
        if (splits.length != 3) throw new BadRequestException("Invalid request line");

        requestBuilder.setMethod(splits[0])
                .setPath(splits[1])
                .setVersion(splits[2]);

        Map<String, String> headers = readHeaders(reader);
        headers.forEach(requestBuilder::addHeader);

        if (headers.get("content-length") != null) {
            int len;
            try {
                len = Integer.parseInt(headers.get("content-length"));
            } catch (NumberFormatException e) {
                throw new BadRequestException("Content-Length is not a number");
            }
            String body = readBody(reader, len);
            requestBuilder.appendBody(body.getBytes(StandardCharsets.UTF_8));
        }
        return requestBuilder.build();
    }

    private static Map<String, String> readHeaders(BufferedReader reader) throws IOException, BadRequestException {
        Map<String, String> headers = new HashMap<>();
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            int index = line.indexOf(":");
            if (index == -1) {
                throw new BadRequestException("Invalid header line");
            }

            String key = line.substring(0, index).trim().toLowerCase();
            String value = line.substring(index + 1).trim();
            headers.put(key, value);
        }
        return headers;
    }

    private static String readBody(BufferedReader reader, int len) throws IOException {
        int totalRead = 0;
        char[] chars = new char[len];
        while (totalRead < len) {
            int r = reader.read(chars, totalRead, len - totalRead);
            if (r == -1) {
                break;
            }
            totalRead += r;
        }
        return new String(chars, 0, totalRead);
    }
}
