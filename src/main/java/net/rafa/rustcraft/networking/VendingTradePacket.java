package net.rafa.rustcraft.networking;


import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.rafa.rustcraft.RustCraft;
import net.rafa.rustcraft.screen.custom.VendingMachineScreenHandler;

/**
 * Packet sent from client → server when the player clicks a "BUY" button.
 * Uses the Fabric 1.21 CustomPayload API.
 */
public record VendingTradePacket(int tradeIndex) implements CustomPayload {

    public static final CustomPayload.Id<VendingTradePacket> ID =
            new CustomPayload.Id<>(Identifier.of(RustCraft.MOD_ID, "vending_trade"));

    // Codec: reads/writes the single int field
    public static final PacketCodec<PacketByteBuf, VendingTradePacket> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.INTEGER, VendingTradePacket::tradeIndex,
                    VendingTradePacket::new
            );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    // ── Registration (call these during mod init) ───────────────────

    /**
     * Register the payload type on BOTH sides.
     * Call from your common initializer (RustCraft.onInitialize).
     */
    public static void registerPayload() {
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
    }

    /**
     * Register the server-side receiver.
     * Call from your common initializer (RustCraft.onInitialize).
     */
    public static void registerReceiver() {
        ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) -> {
            int tradeIndex = payload.tradeIndex();
            context.server().execute(() -> {
                if (context.player().currentScreenHandler
                        instanceof VendingMachineScreenHandler vendingHandler) {
                    vendingHandler.executeTrade(context.player(), tradeIndex);
                }
            });
        });
    }
}
