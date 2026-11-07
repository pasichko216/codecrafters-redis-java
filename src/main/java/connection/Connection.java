package connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;

public class Connection {

    private static final Logger log = LoggerFactory.getLogger(Connection.class);
    private static final int INBOUND_BUFFER_SIZE = 4096;

    private final int id;
    private final SocketChannel channel;
    private final ByteBuffer inbound = ByteBuffer.allocate(INBOUND_BUFFER_SIZE);
    private final Deque<ByteBuffer> outbound = new ArrayDeque<>();
    private SelectionKey key;

    public Connection(int id, SocketChannel channel) {
        this.id = id;
        this.channel = channel;
    }

    public void register(Selector selector) throws IOException {
        channel.configureBlocking(false);
        key = channel.register(selector, SelectionKey.OP_READ, this);
    }

    public void send(String response) throws IOException {
        outbound.add(ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8)));
        flush();
    }

    public void flush() throws IOException {
        while (!outbound.isEmpty()) {
            ByteBuffer head = outbound.peek();
            channel.write(head);
            if (head.hasRemaining()) {
                key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
                return;
            }
            outbound.poll();
        }
        key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
    }

    public void close() {
        try {
            channel.close();
            log.info("Client #{} connection closed", id);
        } catch (IOException e) {
            log.warn("Client #{} failed to close cleanly: {}", id, e.getMessage());
        }
    }

    public int id() {
        return id;
    }

    public SocketChannel channel() {
        return channel;
    }

    public ByteBuffer inbound() {
        return inbound;
    }
}