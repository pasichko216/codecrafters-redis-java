package server;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

@Slf4j
public class RedisServer {

    private final int port;
    private final ClientHandler clientHandler = new ClientHandler();
    private int nextClientId = 1;

    public RedisServer(int port) {
        this.port = port;
    }

    public void start() {
        try (Selector selector = Selector.open();
             ServerSocketChannel serverChannel = ServerSocketChannel.open()) {

            serverChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            serverChannel.bind(new InetSocketAddress(port));
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            log.info("Redis server listening on port {}", port);

            runEventLoop(selector, serverChannel);

        } catch (IOException e) {
            log.error("Server failed: {}", e.getMessage(), e);
        }
    }

    private void runEventLoop(Selector selector, ServerSocketChannel serverChannel) throws IOException {
        while (true) {
            log.debug("Waiting for events...");
            selector.select();
            log.debug("Woke up, ready events: {}", selector.selectedKeys().size());

            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                dispatch(key, selector, serverChannel);
            }
        }
    }

    private void dispatch(SelectionKey key, Selector selector, ServerSocketChannel serverChannel) {
        try {
            if (key.isAcceptable()) {
                accept(serverChannel, selector);
            } else if (key.isReadable()) {
                clientHandler.handle(key);
            }
        } catch (IOException e) {
            log.warn("Client #{} error: {}", key.attachment(), e.getMessage());
            clientHandler.close(key);
        }
    }

    private void accept(ServerSocketChannel serverChannel, Selector selector) throws IOException {
        SocketChannel client = serverChannel.accept();
        if (client == null) {
            return;
        }
        client.configureBlocking(false);

        int clientId = nextClientId++;
        client.register(selector, SelectionKey.OP_READ, clientId);

        log.info("Client #{} connected from {}", clientId, client.getRemoteAddress());
    }
}