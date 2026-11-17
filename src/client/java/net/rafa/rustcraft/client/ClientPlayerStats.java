package net.rafa.rustcraft.client;

import net.rafa.rustcraft.data.DisplayedStats;

/**
 * The player stats as last sent by the server. Kept outside the player entity because the client
 * creates a new player entity on respawn and dimension change.
 */
public final class ClientPlayerStats {
    private static DisplayedStats stats = DisplayedStats.DEFAULT;

    private ClientPlayerStats() {}

    public static DisplayedStats stats() { return stats; }

    public static int thirst() { return stats.thirst(); }

    public static void set(DisplayedStats value) { stats = value; }

    public static void reset() { stats = DisplayedStats.DEFAULT; }
}
