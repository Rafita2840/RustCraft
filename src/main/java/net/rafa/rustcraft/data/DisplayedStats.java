package net.rafa.rustcraft.data;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

/**
 * The part of {@link RustPlayerData} the client needs for its HUD. This is what the sync packet carries, and
 * {@link PlayerStats#set} only sends it when it changed (hidden values like the thirst exhaustion never are).
 * <p>
 * A new displayed stat (radiation, bleeding, cold...) is one more field here, one more entry in {@link #CODEC},
 * and a line in {@link RustPlayerData#displayed()}.
 */
public record DisplayedStats(int thirst) {

    /** What the client assumes before the server has sent anything. */
    public static final DisplayedStats DEFAULT = new DisplayedStats(RustPlayerData.MAX_THIRST);

    public static final PacketCodec<ByteBuf, DisplayedStats> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, DisplayedStats::thirst,
            DisplayedStats::new
    );
}
