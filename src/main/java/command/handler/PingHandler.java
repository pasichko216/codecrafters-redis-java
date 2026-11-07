package command.handler;

import command.Command;
import command.CommandType;
import connection.Connection;
import protocol.RespWriter;

public class PingHandler implements CommandHandler {

    @Override
    public CommandType type() {
        return CommandType.PING;
    }

    @Override
    public String handle(Command command, Connection connection) {
        return RespWriter.PONG;
    }
}