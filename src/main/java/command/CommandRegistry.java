package command;

import command.handler.EchoHandler;
import command.handler.PingHandler;

import java.util.List;

public final class CommandRegistry {

    private CommandRegistry() {}

    public static CommandDispatcher createDispatcher() {
        return new CommandDispatcher(List.of(
                new PingHandler(),
                new EchoHandler()
        ));
    }
}