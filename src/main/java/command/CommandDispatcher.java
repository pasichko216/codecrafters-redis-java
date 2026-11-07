package command;

import command.handler.CommandHandler;
import connection.Connection;
import protocol.ErrorCode;
import protocol.RespWriter;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class CommandDispatcher {

    private final Map<CommandType, CommandHandler> handlers = new EnumMap<>(CommandType.class);

    public CommandDispatcher(List<CommandHandler> handlers) {
        for (CommandHandler handler : handlers) {
            if (this.handlers.putIfAbsent(handler.type(), handler) != null) {
                throw new IllegalStateException("Duplicate command handler: " + handler.type());
            }
        }
    }

    public String dispatch(Command command, Connection connection) {
        CommandHandler handler = CommandType.fromName(command.name())
                .map(handlers::get)
                .orElse(null);

        if (handler == null) {
            return RespWriter.error(ErrorCode.ERR, "unknown command '" + command.name() + "'");
        }
        return handler.handle(command, connection);
    }
}