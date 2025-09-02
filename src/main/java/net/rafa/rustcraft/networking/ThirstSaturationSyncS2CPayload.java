package net.rafa.rustcraft.networking;

import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.rafa.rustcraft.RustCraft;

public record ThirstSaturationSyncS2CPayload(int thirstValue) implements CustomPayload {
    public static final Identifier ID = Identifier.of(RustCraft.MOD_ID, "thirst_sync");
    public static final Id<ThirstSaturationSyncS2CPayload> PAYLOAD_ID =
            new Id<>(ID);

    @Override
    public Id<? extends CustomPayload> getId() {
        return PAYLOAD_ID;
    }
}
