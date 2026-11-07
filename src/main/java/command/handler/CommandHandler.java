package command.handler;

import command.Command;
import command.CommandType;
import connection.Connection;

public interface CommandHandler {

    CommandType type();

    String handle(Command command, Connection connection);
}