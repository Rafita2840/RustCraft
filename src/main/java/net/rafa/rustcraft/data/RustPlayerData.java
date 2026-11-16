package net.rafa.rustcraft.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Everything the mod stores per player. Saved with the player through a Fabric data attachment.
 * New stats (radiation, bleeding, ...) are added here as new fields with a default, so old saves still load.
 */
public record RustPlayerData(int thirst, int thirstExhaustion) {

    public static final int MAX_THIRST = 20;
    /** Thirst of a brand new player. */
    public static final int START_THIRST = 20;
    /** Thirst after dying. */
    public static final int RESPAWN_THIRST = 10;
    /** When the hidden exhaustion counter reaches this, thirst drops by 1. */
    public static final int THIRST_EXHAUSTION_LIMIT = 5000;

    public static final RustPlayerData DEFAULT = new RustPlayerData(START_THIRST, 0);

    public static final Codec<RustPlayerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("thirst", START_THIRST).forGetter(RustPlayerData::thirst),
            Codec.INT.optionalFieldOf("thirst_exhaustion", 0).forGetter(RustPlayerData::thirstExhaustion)
    ).apply(instance, RustPlayerData::new));

    public RustPlayerData withThirst(int thirst) {
        return new RustPlayerData(Math.max(0, Math.min(thirst, MAX_THIRST)), thirstExhaustion);
    }

    public RustPlayerData withThirstExhaustion(int thirstExhaustion) {
        return new RustPlayerData(thirst, thirstExhaustion);
    }
}
