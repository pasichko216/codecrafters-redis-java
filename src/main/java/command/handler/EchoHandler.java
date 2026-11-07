package command.handler;

import command.Command;
import command.CommandType;
import connection.Connection;
import protocol.ErrorCode;
import protocol.RespWriter;

public class EchoHandler implements CommandHandler {

    @Override
    public CommandType type() {
        return CommandType.ECHO;
    }

    @Override
    public String handle(Command command, Connection connection) {
        if (command.args().size() != 1) {
            return RespWriter.error(ErrorCode.ERR, "wrong number of arguments for 'echo' command");
        }
        return RespWriter.bulkString(command.args().getFirst());
    }
}