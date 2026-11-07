import command.CommandDispatcher;
import command.CommandRegistry;
import config.ServerConfig;
import server.ClientHandler;
import server.RedisServer;

void main(String[] args) {
    ServerConfig config = ServerConfig.fromArgs(args);
    CommandDispatcher dispatcher = CommandRegistry.createDispatcher();

    new RedisServer(config, new ClientHandler(dispatcher)).start();
}