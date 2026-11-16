package net.rafa.rustcraft.networking;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.rafa.rustcraft.RustCraft;

/** Server to client: the player stats that the HUD displays. Only sent when they change. */
public record PlayerStatsSyncS2CPayload(int thirst) implements CustomPayload {
    public static final Identifier ID = Identifier.of(RustCraft.MOD_ID, "player_stats_sync");
    public static final CustomPayload.Id<PlayerStatsSyncS2CPayload> PAYLOAD_ID = new CustomPayload.Id<>(ID);
    public static final PacketCodec<PacketByteBuf, PlayerStatsSyncS2CPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, PlayerStatsSyncS2CPayload::thirst, PlayerStatsSyncS2CPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return PAYLOAD_ID;
    }
}
