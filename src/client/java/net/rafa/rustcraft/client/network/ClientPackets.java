package net.rafa.rustcraft.client.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.rafa.rustcraft.networking.VendingTradePacket;

/** Client -> server packet senders. Lives in the client source set only. */
public final class ClientPackets {
    private ClientPackets() {}

    /** Sent when the BUY button of a vending machine trade is clicked. */
    public static void sendVendingTrade(int tradeIndex) {
        ClientPlayNetworking.send(new VendingTradePacket(tradeIndex));
    }
}
