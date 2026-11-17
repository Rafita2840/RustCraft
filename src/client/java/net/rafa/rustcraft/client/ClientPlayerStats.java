package net.rafa.rustcraft.client;

import net.rafa.rustcraft.data.RustPlayerData;

/**
 * The player stats as last sent by the server. Kept outside the player entity because the client
 * creates a new player entity on respawn and dimension change.
 */
public final class ClientPlayerStats {
    private static int thirst = RustPlayerData.MAX_THIRST;

    private ClientPlayerStats() {}

    public static int thirst() { return thirst; }

    public static void setThirst(int value) { thirst = value; }

    public static void reset() { thirst = RustPlayerData.MAX_THIRST; }
}
