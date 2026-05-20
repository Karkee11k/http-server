package com.practice;

import com.practice.config.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

public class HttpServer {
    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);
    
    public static void main(String[] args) {
        var config = new ServerConfig(8080);
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
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ, ConnectionContext.getInstance());
                    } else if (selectionKey.isReadable()) {
                        var connectionContext = (ConnectionContext) selectionKey.attachment();
                        connectionContext.read(selectionKey);
                    } else if (selectionKey.isWritable()) {
                        var connectionContext = (ConnectionContext) selectionKey.attachment();
                        connectionContext.write(selectionKey);
                    }
                }
                selectionKeys.clear();
            }
        } catch (Throwable t) {
            logger.error("Caught an error", t);
        }
    }
}
