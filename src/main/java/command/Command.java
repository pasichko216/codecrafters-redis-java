package command;

import java.util.List;
import java.util.Locale;

public record Command(String name, List<String> args) {

    public static Command of(List<String> parts) {
        return new Command(
                parts.getFirst().toUpperCase(Locale.ROOT),
                parts.subList(1, parts.size())
        );
    }
}