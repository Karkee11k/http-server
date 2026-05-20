package com.practice;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public class ConnectionContext {
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
    
    public void read(SelectionKey selectionKey) {
        
    }
    
    public void write(SelectionKey selectionKey) {
        
    }
    
    public static ConnectionContext getInstance() {
        return new ConnectionContext(++connectionIdCounter);
    }
}
