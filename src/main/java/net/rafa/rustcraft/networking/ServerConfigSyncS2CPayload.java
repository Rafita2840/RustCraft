package net.rafa.rustcraft.networking;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.rafa.rustcraft.RustCraft;

/** Server to client: the config values the client has to follow. Sent on join and after /rustcraft reload. */
public record ServerConfigSyncS2CPayload(boolean forceLowGamma, float maxGamma) implements CustomPayload {
    public static final Identifier ID = Identifier.of(RustCraft.MOD_ID, "server_config_sync");
    public static final CustomPayload.Id<ServerConfigSyncS2CPayload> PAYLOAD_ID = new CustomPayload.Id<>(ID);
    public static final PacketCodec<PacketByteBuf, ServerConfigSyncS2CPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOL, ServerConfigSyncS2CPayload::forceLowGamma,
            PacketCodecs.FLOAT, ServerConfigSyncS2CPayload::maxGamma,
            ServerConfigSyncS2CPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return PAYLOAD_ID;
    }
}
