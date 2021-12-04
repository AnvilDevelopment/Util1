package us.anvildevelopment.v1.configuration;

public interface Configurator {
    String getProject();
    String getModule();
    String getVersion();
    Boolean save();
}
