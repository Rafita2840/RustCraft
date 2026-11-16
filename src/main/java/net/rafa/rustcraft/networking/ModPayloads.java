package net.rafa.rustcraft.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

/**
 * Registers every network payload (and its server-side receiver) of the mod in one place.
 * Add new payloads here.
 */
public final class ModPayloads {
    private ModPayloads() {}

    public static void register() {
        // Client -> server
        VendingTradePacket.registerPayload();
        VendingTradePacket.registerReceiver();

        // Server -> client
        PayloadTypeRegistry.playS2C().register(PlayerStatsSyncS2CPayload.PAYLOAD_ID, PlayerStatsSyncS2CPayload.CODEC);
    }
}
