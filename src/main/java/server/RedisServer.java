package server;

import config.ServerConfig;
import connection.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class RedisServer {

    private static final Logger log = LoggerFactory.getLogger(RedisServer.class);

    private final ServerConfig config;
    private final ClientHandler clientHandler;
    private int nextClientId = 1;

    public RedisServer(ServerConfig config, ClientHandler clientHandler) {
        this.config = config;
        this.clientHandler = clientHandler;
    }

    public void start() {
        try (Selector selector = Selector.open();
             ServerSocketChannel serverChannel = ServerSocketChannel.open()) {

            serverChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            serverChannel.bind(new InetSocketAddress(config.port()));
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            log.info("Redis server listening on port {}", config.port());

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
        if (!key.isValid()) {
            return;
        }
        try {
            if (key.isAcceptable()) {
                accept(serverChannel, selector);
            } else if (key.isReadable()) {
                clientHandler.handle(key);
            } else if (key.isWritable()) {
                ((Connection) key.attachment()).flush();
            }
        } catch (IOException e) {
            if (key.attachment() instanceof Connection connection) {
                log.warn("Client #{} error: {}", connection.id(), e.getMessage());
                connection.close();
            } else {
                log.error("Accept failed: {}", e.getMessage(), e);
            }
        }
    }

    private void accept(ServerSocketChannel serverChannel, Selector selector) throws IOException {
        SocketChannel client = serverChannel.accept();
        if (client == null) {
            return;
        }

        Connection connection = new Connection(nextClientId++, client);
        connection.register(selector);

        log.info("Client #{} connected from {}", connection.id(), client.getRemoteAddress());
    }
}