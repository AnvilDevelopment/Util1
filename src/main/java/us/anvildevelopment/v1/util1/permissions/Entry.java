package us.anvildevelopment.v1.util1.permissions;

public interface Entry {
    String getName();
    boolean getAllow();
    void setAllow(boolean b);
    Integer getWeight();
}
