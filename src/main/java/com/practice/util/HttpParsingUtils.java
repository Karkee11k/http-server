package com.practice.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HttpParsingUtils {
    private static final byte[] HEADERS_END = {'\r', '\n', '\r', '\n'};
    private static final byte[] LINE_END = {'\r', '\n'};
    
    public static boolean hasCompleteRequest(ByteBuffer buffer) {
        return findSequenceEnd(buffer, HEADERS_END) != -1;
    }
    
    public static String[] readRequestLine(ByteBuffer buffer) {
        var requestLine = Objects.requireNonNull(readLine(buffer));
        return requestLine.split("\\s+");
    }
    
    public static Map<String, String> parseHeaders(ByteBuffer buffer) {
        var headers = new HashMap<String, String>();
        String line;
        while ((line = readLine(buffer)) != null && !line.isEmpty()) {
            int index = line.indexOf(":");
            var key = line.substring(0, index).toLowerCase();
            var value = line.substring(index + 1).trim();
            headers.put(key, value);
        }
        return headers;
    }

    public static int findSequenceEnd(ByteBuffer buffer, byte[] sequence) {
        for (int i = buffer.position(); i <= buffer.limit() - sequence.length; ++i) {
            int j = 0;
            while (j < sequence.length && buffer.get(i + j) == sequence[j]) {
                ++j;
            }
            if (j == sequence.length) {
                return i + j;
            }
        }
        return -1;
    }

    public static String readLine(ByteBuffer buffer) {
        int end = findSequenceEnd(buffer, LINE_END);
        if (end == -1) {
            return null;
        }
        byte[] bytes = new byte[end - buffer.position() - LINE_END.length];
        buffer.get(bytes);
        buffer.position(end);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
