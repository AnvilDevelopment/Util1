package us.anvildevelopment.util.tools.permissions;

/**
 * The weight of an entry can be any integer
 */
public enum DefaultWeights {
    USER(0),
    ADMIN(1),
    OWNER(100);

    DefaultWeights(int i) {
    }
    int i;
}
