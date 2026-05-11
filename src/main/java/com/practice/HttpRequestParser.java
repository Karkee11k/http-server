package com.practice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import static com.practice.HttpRequest.HttpRequestBuilder;

public class HttpRequestParser {
    public static HttpRequest parse(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();

        String requestLine = reader.readLine();
        if (requestLine == null) throw new IOException("Empty request");

        String[] splits = requestLine.split(" ");
        if (splits.length != 3) throw new IOException("Invalid request line");

        String method = splits[0];
        requestBuilder.setMethod(method)
                .setPath(splits[1])
                .setVersion(splits[2]);

        Map<String, String> headers = readHeaders(reader);
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            requestBuilder.addHeader(entry.getKey(), entry.getValue());
        }

        if (hasToReadBody(method) && headers.get("content-length") != null) {
            int len = Integer.parseInt(headers.get("content-length"));
            String body = readBody(reader, len);
            requestBuilder.setBody(body);
        }
        return requestBuilder.build();
    }

    private static Map<String, String> readHeaders(BufferedReader reader) throws IOException {
        Map<String, String> headers = new HashMap<>();
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            int index = line.indexOf(":");
            if (index == -1) continue;

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
    
    private static boolean hasToReadBody(String method) {
        method = method.toLowerCase();
        return method.equals("post") || method.equals("put") || method.equals("patch");
    }
}
