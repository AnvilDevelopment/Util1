package us.anvildevelopment.util.configuration.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import us.anvildevelopment.util.configuration.ConfiguratorServer;

import java.io.IOException;
import java.util.Objects;

@Deprecated(since = "2.0", forRemoval = true)
public class LoginHandler implements HttpHandler {

    private final ConfiguratorServer server;

    public LoginHandler(ConfiguratorServer server) {
        this.server = Objects.requireNonNull(server, "ConfiguratorServer must not be null");
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String remoteAddr = exchange.getRemoteAddress().getAddress().getHostAddress();

        if (server.isBlacklisted(remoteAddr)) {
            exchange.close();
            return;
        }

        String uid = exchange.getRequestHeaders().getFirst("uid");
        if (uid != null && server.isInvalidUser(uid)) {
            exchange.sendResponseHeaders(403, 0);
            exchange.close();
            return;
        }

        // Default: allow (or delegate to next handler)
        exchange.sendResponseHeaders(200, 0);
        exchange.close();
    }
}
