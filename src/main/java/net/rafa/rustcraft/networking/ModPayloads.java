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
        PayloadTypeRegistry.playS2C().register(ThirstSyncS2CPayload.PAYLOAD_ID, ThirstSyncS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ThirstSaturationSyncS2CPayload.PAYLOAD_ID, ThirstSaturationSyncS2CPayload.CODEC);
    }
}
