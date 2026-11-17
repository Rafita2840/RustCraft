package net.rafa.rustcraft.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.rafa.rustcraft.config.RustConfig;

/**
 * Everything the mod stores per player. Saved with the player through a Fabric data attachment.
 * New stats (radiation, bleeding, ...) are added here as new fields with a default, so old saves still load.
 */
public record RustPlayerData(int thirst, int thirstExhaustion) {

    public static final int MAX_THIRST = 20;

    public static final Codec<RustPlayerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("thirst", MAX_THIRST).forGetter(RustPlayerData::thirst),
            Codec.INT.optionalFieldOf("thirst_exhaustion", 0).forGetter(RustPlayerData::thirstExhaustion)
    ).apply(instance, RustPlayerData::new));

    /** Data of a brand new player. Reads the config, so it must not be cached in a static field. */
    public static RustPlayerData initial() {
        return new RustPlayerData(RustConfig.get().thirst.startThirst, 0);
    }

    /** Data after dying. */
    public static RustPlayerData afterDeath() {
        return new RustPlayerData(RustConfig.get().thirst.respawnThirst, 0);
    }

    public RustPlayerData withThirst(int thirst) {
        return new RustPlayerData(Math.max(0, Math.min(thirst, MAX_THIRST)), thirstExhaustion);
    }

    public RustPlayerData withThirstExhaustion(int thirstExhaustion) {
        return new RustPlayerData(thirst, thirstExhaustion);
    }
}
