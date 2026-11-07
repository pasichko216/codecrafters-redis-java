package server;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

import static utils.Constants.*;

@Slf4j
public class ClientHandler {

    public void handle(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        Integer clientId = (Integer) key.attachment();
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        int bytesRead = client.read(buffer);
        if (bytesRead == -1) {
            log.info("Client #{} hung up", clientId);
            close(key);
            return;
        }

        buffer.flip();
        String data = StandardCharsets.UTF_8.decode(buffer).toString();
        log.info("Client #{} sent {} bytes: {}", clientId, bytesRead, visible(data));

        for (String line : data.split(CRLF)) {
            if (line.equalsIgnoreCase(PING)) {
                send(client, clientId, PONG_RESPONSE);
            } else {
                log.debug("Client #{} line ignored: {}", clientId, visible(line));
            }
        }
    }

    public void close(SelectionKey key) {
        Integer clientId = (Integer) key.attachment();
        try {
            key.channel().close();
            log.info("Client #{} connection closed", clientId);
        } catch (IOException e) {
            log.warn("Client #{} failed to close cleanly: {}", clientId, e.getMessage());
        }
    }

    private void send(SocketChannel client, Integer clientId, String response) throws IOException {
        client.write(ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8)));
        log.info("Client #{} <- {}", clientId, visible(response));
    }

    // make \r и \n visible in logs
    private String visible(String text) {
        return text.replace("\r", "\\r").replace("\n", "\\n");
    }
}