package server;

import command.Command;
import command.CommandDispatcher;
import connection.Connection;
import exception.ProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.ErrorCode;
import protocol.RespParser;
import protocol.RespWriter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.List;

public class ClientHandler {

    private static final Logger log = LoggerFactory.getLogger(ClientHandler.class);

    private final RespParser parser = new RespParser();
    private final CommandDispatcher dispatcher;

    public ClientHandler(CommandDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void handle(SelectionKey key) throws IOException {
        Connection connection = (Connection) key.attachment();
        ByteBuffer inbound = connection.inbound();

        int bytesRead = connection.channel().read(inbound);
        if (bytesRead == -1) {
            log.info("Client #{} hung up", connection.id());
            connection.close();
            return;
        }
        log.debug("Client #{} sent {} bytes", connection.id(), bytesRead);

        inbound.flip();
        try {
            processCommands(connection);
        } catch (ProtocolException e) {
            log.warn("Client #{} protocol error: {}", connection.id(), e.getMessage());
            connection.send(RespWriter.error(ErrorCode.ERR, "Protocol error: " + e.getMessage()));connection.close();
            return;
        }
        inbound.compact();

        if (!inbound.hasRemaining()) {
            log.warn("Client #{} request too large", connection.id());
            connection.send(RespWriter.error(ErrorCode.ERR, "Protocol error: request too large"));
            connection.close();
        }
    }

    private void processCommands(Connection connection) throws IOException {
        List<String> parts;
        while ((parts = parser.parse(connection.inbound())) != null) {
            if (parts.isEmpty()) {
                continue;
            }
            Command command = Command.of(parts);
            log.info("Client #{} command: {} {}", connection.id(), command.name(), command.args());

            String response = dispatcher.dispatch(command, connection);
            log.info("Client #{} <- {}", connection.id(), visible(response));
            connection.send(response);
        }
    }

    private String visible(String text) {
        return text.replace("\r", "\\r").replace("\n", "\\n");
    }
}