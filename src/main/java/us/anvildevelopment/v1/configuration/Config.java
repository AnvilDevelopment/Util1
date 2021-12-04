package us.anvildevelopment.v1.configuration;

public class Config implements Configurator{
    public static Boolean setup = false;
    public static Integer port = 16034;
    public static Integer pin;
    public static String username;
    public static String password;
    public static String sql_username = "Admin";
    public static String sql_password = "";
    public static String sql_address = "mysqlx://localhost:33060";

    @Override
    public String getProject() {
        return "Configurator";
    }

    @Override
    public String getModule() {
        return "Config";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public Boolean save() {
        return ConfiguratorServer.storeConfig(this);
    }
}
