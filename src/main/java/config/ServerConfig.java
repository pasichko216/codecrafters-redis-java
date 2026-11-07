package config;

public record ServerConfig(int port) {

    public static final int DEFAULT_PORT = 6379;

    public static ServerConfig fromArgs(String[] args) {
        int port = DEFAULT_PORT;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--port")) {
                port = Integer.parseInt(valueAfter(args, i));
                i++;
            }
        }
        return new ServerConfig(port);
    }

    private static String valueAfter(String[] args, int index) {
        if (index + 1 >= args.length) {
            throw new IllegalArgumentException("Missing value for " + args[index]);
        }
        return args[index + 1];
    }
}