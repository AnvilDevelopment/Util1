package us.anvildevelopment.util.configuration;

public interface Configurator {
    String getProject();
    String getModule();
    String getVersion();
    Boolean save();
}
