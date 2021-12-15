package us.anvildevelopment.v1.configuration;

public class User implements Configurator {
    public String uid;
    public String auth;

    @Override
    public String getProject() {
        return "Configurator";
    }

    @Override
    public String getModule() {
        return "Users";
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
