package net.rafa.rustcraft.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.rafa.rustcraft.config.RustConfig;

/**
 * Everything the mod stores per player. Saved with the player through a Fabric data attachment.
 * New stats (radiation, bleeding, ...) are added here as new fields with a default, so old saves still load.
 *
 * @param thirstExhaustion hidden counter; thirst drops by 1 each time it reaches the config limit.
 *                         A float so drain multipliers (hot biomes, the Nether) aren't rounded away.
 */
public record RustPlayerData(int thirst, float thirstExhaustion) {

    public static final int MAX_THIRST = 20;

    // Codec.FLOAT also reads the whole numbers that saves from before P1.1 contain
    public static final Codec<RustPlayerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("thirst", MAX_THIRST).forGetter(RustPlayerData::thirst),
            Codec.FLOAT.optionalFieldOf("thirst_exhaustion", 0f).forGetter(RustPlayerData::thirstExhaustion)
    ).apply(instance, RustPlayerData::new));

    /** Data of a brand new player. Reads the config, so it must not be cached in a static field. */
    public static RustPlayerData initial() {
        return new RustPlayerData(RustConfig.get().thirst.startThirst, 0);
    }

    /** Data after dying. */
    public static RustPlayerData afterDeath() {
        return new RustPlayerData(RustConfig.get().thirst.respawnThirst, 0);
    }

    /** The values the client displays. Only a change in these is sent to the client. */
    public DisplayedStats displayed() {
        return new DisplayedStats(thirst);
    }

    public RustPlayerData withThirst(int thirst) {
        return new RustPlayerData(Math.max(0, Math.min(thirst, MAX_THIRST)), thirstExhaustion);
    }

    public RustPlayerData withThirstExhaustion(float thirstExhaustion) {
        return new RustPlayerData(thirst, thirstExhaustion);
    }
}
