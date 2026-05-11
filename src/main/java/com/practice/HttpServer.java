package com.practice;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

public class HttpServer {
    public static void main(String[] args) {
        try (var serverSocket = new ServerSocket(8080);
             var executor = Executors.newFixedThreadPool(10)) {
            while (true) {
                var clientSocket = serverSocket.accept();
                executor.execute(() -> handle(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void handle(Socket client) {
        try {
            InputStream in = client.getInputStream();
            HttpRequest request = HttpRequestParser.parse(in);

            String response = """
                    HTTP/1.1 200 OK\r
                    Content-Type: text/plain\r
                    \r
                    Hello from Server
                    """;

            OutputStream out = client.getOutputStream();
            if (request.getPath().equals("/hello")) {
                out.write(response.getBytes());
            } else {
                out.write("HTTP/1.1 404\r".getBytes());
            }
            out.flush();

            out.close();
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
