package com.practice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

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
            selectionKey.cancel();
            logger.info("Closed connection {}", this.connectionId);
        } else if (r > 0) {
            readBuffer.flip();
            selectionKey.interestOps(SelectionKey.OP_WRITE);
        }
    }
    
    public void write(SelectionKey selectionKey) throws IOException {
        var channel = (SocketChannel) selectionKey.channel();
        channel.write(readBuffer);
        if (!readBuffer.hasRemaining()) {
            readBuffer.clear();
            selectionKey.interestOps(SelectionKey.OP_READ);
        }
    }
    
    public static ConnectionContext getInstance() {
        return new ConnectionContext(++connectionIdCounter);
    }
}
