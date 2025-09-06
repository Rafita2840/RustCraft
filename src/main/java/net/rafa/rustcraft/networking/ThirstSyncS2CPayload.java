package net.rafa.rustcraft.networking;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.rafa.rustcraft.RustCraft;

public record ThirstSyncS2CPayload(int thirstValue) implements CustomPayload {
    public static final Identifier ID = Identifier.of(RustCraft.MOD_ID, "thirst_sync");
    public static final CustomPayload.Id<ThirstSyncS2CPayload> PAYLOAD_ID =
            new CustomPayload.Id<>(ID);
    public static final PacketCodec<PacketByteBuf, ThirstSyncS2CPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, ThirstSyncS2CPayload::thirstValue, ThirstSyncS2CPayload::new
    );


    @Override
    public Id<? extends CustomPayload> getId() {
        return PAYLOAD_ID;
    }

    public static String getPath(){
        return "thirst";
    }
}
