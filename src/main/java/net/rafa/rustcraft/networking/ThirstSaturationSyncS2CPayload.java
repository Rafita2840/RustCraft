package net.rafa.rustcraft.networking;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.rafa.rustcraft.RustCraft;

public record ThirstSaturationSyncS2CPayload(int saturationValue) implements CustomPayload {
    public static final Identifier ID = Identifier.of(RustCraft.MOD_ID, "thirst_saturation_sync");
    public static final CustomPayload.Id<ThirstSaturationSyncS2CPayload> PAYLOAD_ID =
            new CustomPayload.Id<>(ID);
    public static final PacketCodec<PacketByteBuf, ThirstSaturationSyncS2CPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, ThirstSaturationSyncS2CPayload::saturationValue, ThirstSaturationSyncS2CPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return PAYLOAD_ID;
    }

    public static String getPath(){
        return "thirst_saturation";
    }
}
