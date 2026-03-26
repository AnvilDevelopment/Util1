package us.anvildevelopment.util.tools.permissions;

public interface Entry {
    String getName();
    boolean getAllow();
    void setAllow(boolean b);
    Integer getWeight();
}
