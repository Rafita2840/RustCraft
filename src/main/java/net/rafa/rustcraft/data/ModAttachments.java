package net.rafa.rustcraft.data;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.util.Identifier;
import net.rafa.rustcraft.RustCraft;

/** Data attachments of the mod. */
public final class ModAttachments {
    private ModAttachments() {}

    /** All per-player stats. Saved with the player; synced to the client by {@link PlayerStats}. */
    public static final AttachmentType<RustPlayerData> PLAYER_DATA = AttachmentRegistry.<RustPlayerData>create(
            Identifier.of(RustCraft.MOD_ID, "player_data"),
            builder -> builder
                    .initializer(RustPlayerData::initial)
                    .persistent(RustPlayerData.CODEC));

    /** Call once from onInitialize so the attachment is registered before any world loads. */
    public static void init() {
        RustCraft.LOGGER.info("Registering Mod Attachments for " + RustCraft.MOD_ID);
    }
}
