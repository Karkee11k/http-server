package com.practice;

import com.practice.exceptions.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HttpServer {
    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);
    
    public static void main(String[] args) {
        try (var serverSocket = new ServerSocket(8080);
             var executor = Executors.newFixedThreadPool(10)) {
            while (true) {
                var clientSocket = serverSocket.accept();
                executor.execute(() -> handle(clientSocket));
            }
        } catch (IOException e) {
            logger.error("Caught exception on server start", e);
        }
    }
    
    public static void handle(Socket client) {
        try (client; InputStream in = client.getInputStream(); OutputStream out = client.getOutputStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            try {
                client.setSoTimeout((int) TimeUnit.SECONDS.toMillis(20));
                while (true) {
                    HttpRequest request;
                    try {
                        request = HttpRequestParser.parse(reader);
                    } catch (SocketTimeoutException e) {
                        logger.info("Connection closed after timeout. Thread released: {}", Thread.currentThread().getName());
                        break;
                    }
                    
                    HttpResponse response = handleRequest(request);
                    boolean close = shouldCloseConnection(request);
                    if (close) {
                        response.setHeader("Connection", "close");
                    } else {
                        response.setHeader("Connection", "keep-alive");
                    }
                    
                    out.write(response.toBytes());
                    out.flush();
                    
                    if (close) {
                        break;
                    }
                }
            } catch (IOException e) {
                // Client disconnected or stream ended — expected for keep-alive
            } catch (BadRequestException e) {
                sendBadRequest(out);
            }
        } catch (IOException e) {
            logger.error("Caught an error at connection handling", e);
        }
    }

    private static HttpResponse handleRequest(HttpRequest request) {
        HttpResponse response = new HttpResponse();
        if (request.getPath().equals("/hello")) {
            String html = "<h1>Hello from Server</h1>";
            response.setStatus(200, "OK");
            response.setHeader("Content-Type", "text/html");
            response.setBody(html);
        } else {
            response.setStatus(404, "Not Found");
            response.setHeader("Content-Type", "text/plain");
            response.setBody("Not Found");
        }
        return response;
    }
    
    private static void sendBadRequest(OutputStream out) {
        try {
            HttpResponse errorResponse = new HttpResponse();
            errorResponse.setStatus(400, "Bad Request");
            errorResponse.setHeader("Connection", "close");
            errorResponse.setBody("Bad Request");
            out.write(errorResponse.toBytes());
            out.flush();
        } catch (IOException ignored) {}
    }
    
    private static boolean shouldCloseConnection(HttpRequest request) {
        String connection = request.getHeader("connection");
        return connection != null && connection.equalsIgnoreCase("close");
    }
}
