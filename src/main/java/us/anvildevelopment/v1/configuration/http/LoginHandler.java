package us.anvildevelopment.v1.configuration.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import us.anvildevelopment.v1.configuration.ConfiguratorServer;
import us.anvildevelopment.v1.configuration.User;

import java.io.IOException;

public class LoginHandler implements HttpHandler {
    public ConfiguratorServer instance;
    public LoginHandler(ConfiguratorServer cs) {

    }
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (ConfiguratorServer.blacklist.contains(exchange.getRemoteAddress().getAddress().toString())) {
            exchange.close();
    } else {
            String uid = exchange.getRequestHeaders().getFirst("uid");
            if (instance.userMap.containsKey(uid)) {
                String auth = instance.userMap.get(uid);
                if (auth.equalsIgnoreCase("INVALID")) {
                    exchange.sendResponseHeaders(403, 0);
                }
            }
        }
}
