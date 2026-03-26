module us.anvildevelopment.util {
    requires java.sql;
    requires com.fasterxml.jackson.databind;
    requires java.net.http;
    requires jdk.httpserver;
    requires com.mysql.cj;

    exports us.anvildevelopment.util.configuration;
    exports us.anvildevelopment.util.tools.database;
    exports us.anvildevelopment.util.tools.exceptions;
    exports us.anvildevelopment.util.tools.permissions;
    exports us.anvildevelopment.util.tools.database.annotations;
    exports us.anvildevelopment.util.tools.security;
}