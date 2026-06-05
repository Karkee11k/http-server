package com.practice;

import com.practice.exceptions.BadRequestException;
import com.practice.http.HttpStatus;
import com.practice.routing.Router;
import com.practice.util.HttpParsingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.util.Map;

public class ConnectionContext {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionContext.class);
    private static final int KB = 1024;
    private static final int MAX_PAYLOAD_SIZE = 8 * KB;
    private static final int BUFFER_SIZE = 16 * KB;
    private static int connectionIdCounter = 0;
    enum State { READING_HEADERS, READING_BODY }
    
    private State currentState = State.READING_HEADERS;
    private final int connectionId;
    private final Router router;
    private final ByteBuffer readBuffer;
    private ByteBuffer writeBuffer;
    private HttpRequest.HttpRequestBuilder requestBuilder;
    private int expectedBodylength = -1;
    private int bodyBytesRead = 0;
    private boolean closeAfterWrite = false;
    
    private ConnectionContext(int connectionId, Router router) {
        this.router = router;
        this.connectionId = connectionId;
        this.readBuffer   = ByteBuffer.allocate(BUFFER_SIZE);
        this.requestBuilder = new HttpRequest.HttpRequestBuilder();
    }
    
    public void read(SelectionKey selectionKey) throws IOException {
        var channel = (ByteChannel) selectionKey.channel();
        int r = channel.read(readBuffer);
        if (r == -1) {
            channel.close();
            selectionKey.cancel();
            logger.info("Closed connection {}", this.connectionId);
            return;
        } 
        
        if (r == 0) {
            return;
        }
        
        readBuffer.flip();
        if (currentState == State.READING_HEADERS) {
            handleHeaders(selectionKey);
        } else if (currentState == State.READING_BODY) {
            handleBody(selectionKey);
        }
    }
    
    // for your future self, action is better than regret
    public void write(SelectionKey selectionKey) throws IOException {
        var channel = (ByteChannel) selectionKey.channel();
        channel.write(this.writeBuffer);
        if (!writeBuffer.hasRemaining()) {
            writeBuffer.clear();
            
            if (closeAfterWrite) {
                channel.close();
                selectionKey.cancel();
            } else {
                selectionKey.interestOps(SelectionKey.OP_READ);
            }
        }
    }
    
    // Todo: Add check for headers 
    private void handleHeaders(SelectionKey selectionKey) {
        if (!HttpParsingUtils.hasCompleteRequest(readBuffer)) {
            readBuffer.compact();
            return;
        }
        
        var requestLine = HttpParsingUtils.readRequestLine(readBuffer);
        if (requestLine.length != 3) {
            sendError(selectionKey, HttpStatus.BAD_REQUEST);
            return;
        }

        Map<String, String> headers;
        try {
            headers = HttpParsingUtils.parseHeaders(readBuffer);
        } catch (BadRequestException e) {
            sendError(selectionKey, HttpStatus.BAD_REQUEST);
            return;
        }

        requestBuilder.setMethod(requestLine[0])
                .setPath(requestLine[1])
                .setVersion(requestLine[2]);
        headers.forEach(requestBuilder::addHeader);

        var contentLength = headers.get("content-length");
        if (contentLength == null) {
            finishRequest(selectionKey);
            return;
        }
        
        try {
            expectedBodylength = Integer.parseInt(contentLength);
        } catch (NumberFormatException e) {
            sendError(selectionKey, HttpStatus.BAD_REQUEST);
            return;
        }
        
        if (expectedBodylength > MAX_PAYLOAD_SIZE) {
            sendError(selectionKey, HttpStatus.PAYLOAD_TOO_LARGE);
            return;
        }
        
        if (expectedBodylength < 0) {
            sendError(selectionKey, HttpStatus.BAD_REQUEST);
            return;
        }
        
        if (expectedBodylength > 0)  {
            byte[] chunk = new byte[Math.min(readBuffer.remaining(), expectedBodylength)];
            readBuffer.get(chunk);
            bodyBytesRead += chunk.length;
            requestBuilder.appendBody(chunk);
            
            if (bodyBytesRead < expectedBodylength) {
                currentState = State.READING_BODY;
                readBuffer.compact();
                return;
            }
        }
        finishRequest(selectionKey);
    }
    
    private void handleBody(SelectionKey selectionKey) {
        int len = Math.min(expectedBodylength - bodyBytesRead, readBuffer.remaining());
        byte[] chunk = new byte[len];
        readBuffer.get(chunk);
        bodyBytesRead += chunk.length;
        requestBuilder.appendBody(chunk);
        if (bodyBytesRead >= expectedBodylength) {
            finishRequest(selectionKey);
        } else {
            readBuffer.compact();
        }
    }
    
    private void finishRequest(SelectionKey selectionKey) {
        var request = requestBuilder.build();
        HttpResponse response;
        try {
            response = this.router.dispatch(request);
        } catch (Exception e) {
            logger.error("Handler error on connection id: {}", this.connectionId, e);
            sendError(selectionKey, HttpStatus.INTERNAL_SERVER_ERROR);
            return;
        }
        this.closeAfterWrite = response.shouldClose();
        this.writeBuffer = ByteBuffer.wrap(response.toBytes());
        
        resetState();
        selectionKey.interestOps(SelectionKey.OP_WRITE);
    }
    
    private void sendError(SelectionKey selectionKey, HttpStatus status) {
        var response = new HttpResponse();
        response.setStatus(status);
        response.setHeader("Content-Type", "text/plain");
        response.setBody(status.text());
        response.setHeader("Connection", "close");
        this.closeAfterWrite = true;
        this.writeBuffer = ByteBuffer.wrap(response.toBytes());
        selectionKey.interestOps(SelectionKey.OP_WRITE);
    }
    
    private void resetState() {
        requestBuilder = new HttpRequest.HttpRequestBuilder();
        readBuffer.compact();
        currentState = State.READING_HEADERS;
        bodyBytesRead = 0;
        expectedBodylength = -1;
    }

    public static ConnectionContext getInstance(Router router) {
        return new ConnectionContext(++connectionIdCounter, router);
    }
}
