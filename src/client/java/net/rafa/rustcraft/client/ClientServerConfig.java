package net.rafa.rustcraft.client;

/**
 * The server config values this client has to follow, as last sent by the server.
 * Back to the defaults (nothing forced) when not connected.
 */
public final class ClientServerConfig {
    private static boolean forceLowGamma = false;
    private static float maxGamma = 0.5f;

    private ClientServerConfig() {}

    public static boolean forceLowGamma() { return forceLowGamma; }

    public static float maxGamma() { return maxGamma; }

    public static void set(boolean forceLowGamma, float maxGamma) {
        ClientServerConfig.forceLowGamma = forceLowGamma;
        ClientServerConfig.maxGamma = maxGamma;
    }

    public static void reset() {
        set(false, 0.5f);
    }
}
