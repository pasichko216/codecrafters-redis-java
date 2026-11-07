package command;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum CommandType {
    PING,
    ECHO;

    private static final Map<String, CommandType> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(CommandType::name, Function.identity()));

    public static Optional<CommandType> fromName(String name) {
        return Optional.ofNullable(BY_NAME.get(name.toUpperCase(Locale.ROOT)));
    }
}