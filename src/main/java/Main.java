import server.RedisServer;

import static utils.Constants.DEFAULT_PORT;

void main() {
    new RedisServer(DEFAULT_PORT).start();
}
