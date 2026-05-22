package com.practice;

import com.practice.util.HttpParsingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ConnectionContext {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionContext.class);
    private static final int KB = 1024;
    private static final int BUFFER_SIZE = 16 * KB;
    private static int connectionIdCounter = 0;
    
    private final int connectionId;
    private final ByteBuffer readBuffer;
    private final ByteBuffer writeBuffer;
    
    private ConnectionContext(int connectionId) {
        this.connectionId = connectionId;
        this.readBuffer   = ByteBuffer.allocate(BUFFER_SIZE);
        this.writeBuffer  = ByteBuffer.allocate(BUFFER_SIZE);
    }
    
    public void read(SelectionKey selectionKey) throws IOException {
        var channel = (SocketChannel) selectionKey.channel();
        int r = channel.read(readBuffer);
        if (r == -1) {
            channel.close();
            logger.info("Closed connection {}", this.connectionId);
            return;
        } 
        
        if (r == 0) {
            return;
        }
        
        readBuffer.flip();
        if (HttpParsingUtils.hasCompleteRequest(readBuffer)) {
            var requestLine = HttpParsingUtils.readRequestLine(readBuffer);
            var headers = HttpParsingUtils.parseHeaders(readBuffer);
            var builder = new HttpRequest.HttpRequestBuilder();
            builder.setMethod(requestLine[0])
                    .setPath(requestLine[1])
                    .setVersion(requestLine[2]);
            headers.forEach(builder::addHeader);
            var request = builder.build();
            var response = handle(request);
            this.writeBuffer.put(response.toBytes());
            this.writeBuffer.flip();
            selectionKey.interestOps(SelectionKey.OP_WRITE);
        } else {
            readBuffer.compact();
        }
    }
    
    // for your future self, action is better than regret
    public void write(SelectionKey selectionKey) throws IOException {
        var channel = (SocketChannel) selectionKey.channel();
        channel.write(this.writeBuffer);
        if (!writeBuffer.hasRemaining()) {
            writeBuffer.clear();
            readBuffer.clear();
            selectionKey.interestOps(SelectionKey.OP_READ);
        }
    }
    
    public HttpResponse handle(HttpRequest request) {
        var response = new HttpResponse();
        if (request.getPath().equals("/hello")) {
            String body = "<h1>Hello from server</h1>";
            response.setStatus(200, "OK");
            response.setHeader("Content-Length", String.valueOf(body.getBytes(StandardCharsets.UTF_8).length));
            response.setHeader("Content-Type", "text/html");
            response.setBody(body);
        } else {
            response.setStatus(404, "Not found");
            response.setBody("Not found");
        }
        response.setHeader("Connection", "keep-alive");
        return response;
    }
    
    public static ConnectionContext getInstance() {
        return new ConnectionContext(++connectionIdCounter);
    }
}
