package net.rafa.rustcraft.client;

/**
 * The server config values and game rules this client has to follow, as last sent by the server.
 * Back to the defaults (nothing forced, thirst on) when not connected.
 */
public final class ClientServerConfig {
    private static boolean forceLowGamma = false;
    private static float maxGamma = 0.5f;
    private static boolean thirstEnabled = true;

    private ClientServerConfig() {}

    public static boolean forceLowGamma() { return forceLowGamma; }

    public static float maxGamma() { return maxGamma; }

    /** The {@code rustcraftThirstEnabled} game rule. */
    public static boolean thirstEnabled() { return thirstEnabled; }

    public static void set(boolean forceLowGamma, float maxGamma, boolean thirstEnabled) {
        ClientServerConfig.forceLowGamma = forceLowGamma;
        ClientServerConfig.maxGamma = maxGamma;
        ClientServerConfig.thirstEnabled = thirstEnabled;
    }

    public static void reset() {
        set(false, 0.5f, true);
    }
}
