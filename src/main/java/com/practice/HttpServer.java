package com.practice;

import com.practice.config.ServerConfig;
import com.practice.http.HttpStatus;
import com.practice.routing.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

public class HttpServer {
    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);
    
    public static void main(String[] args) {
        var router = new Router();
        router.get("/hello", request -> {
            var response = new HttpResponse();
            response.setStatus(HttpStatus.OK);
            response.setHeader("Content-Type", "text/html");
            response.setBody("<h1 style=\"color: red\">Hello From Server</h1>");
            return response;
        });
        
        router.get("/users", request -> {
            var response = new HttpResponse();
            response.setStatus(HttpStatus.OK);
            response.setHeader("Content-Type", "text/html");
            response.setBody("<h1 style=\"color: blue\">Users: karthi, mani, michael, mano</h1>");
            return response;
        });

        router.get("/tasks", request -> {
            var json = """
                    {
                        "tasks": [
                            "buy amla",
                            "go to gym",
                            "pay rent",
                            "wash clothes"
                        ]
                    }
                    """;
            var response = new HttpResponse();
            response.setStatus(HttpStatus.OK);
            response.setHeader("Content-Type", "application/json");
            response.setBody("{\"request_body\": " +  json + "}");
            return response;
        });
        
        router.post("/hello", request -> {
            var response = new HttpResponse();
            response.setStatus(HttpStatus.OK);
            response.setHeader("Content-Type", "application/json");
            var json = request.getBodyAsString();
            response.setBody("{\"request_body\": " +  json + "}");
            return response;
        });
        
        var config = new ServerConfig(8080, router);
        start(config);
    }
    
    public static void start(ServerConfig config) {
        try (var serverSocket = ServerSocketChannel.open(); var selector = Selector.open()) {
            var address = new InetSocketAddress(config.port());
            serverSocket.bind(address);
            serverSocket.configureBlocking(false);
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);
            
            while (true) {
                selector.select();
                var selectionKeys = selector.selectedKeys();
                for (var selectionKey : selectionKeys) {
                    if (selectionKey.isAcceptable()) {
                        var server =  (ServerSocketChannel) selectionKey.channel();
                        var client = server.accept();
                        if (client != null) {
                            client.configureBlocking(false);
                            client.register(selector, SelectionKey.OP_READ, ConnectionContext.getInstance(config.router()));
                        }
                    } else if (selectionKey.isReadable()) {
                        var ctx = (ConnectionContext) selectionKey.attachment();
                        ctx.read(selectionKey);
                    } else if (selectionKey.isWritable()) {
                        var ctx = (ConnectionContext) selectionKey.attachment();
                        ctx.write(selectionKey);
                    }
                }
                selectionKeys.clear();
            }
        } catch (Throwable t) {
            logger.error("Fatal error: Server event loop terminated!", t);
        }
    }
}
