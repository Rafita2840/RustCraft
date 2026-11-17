package net.rafa.rustcraft.thirst;

import net.minecraft.world.Difficulty;
import net.rafa.rustcraft.config.RustConfig;
import net.rafa.rustcraft.data.RustPlayerData;

/** The thirst rules as plain functions (no world or player needed), so they can be tested on their own. */
public final class ThirstMath {
    private ThirstMath() {}

    /**
     * Drain from what the player did this tick, before multipliers (ToDo: still +1, moving +2, running +7,
     * jumping +2, at most 12). Sprinting only counts while actually moving, so sprinting into a wall is idle.
     */
    public static int baseDrain(boolean moving, boolean sprinting, boolean jumped, RustConfig.Thirst config) {
        int amount = config.idleDrain;
        if (moving) {
            amount += config.walkDrain;
            if (sprinting) {
                amount += config.runDrain;
            }
        }
        if (jumped) {
            amount += config.jumpDrain;
        }
        return Math.min(amount, config.maxDrainPerTick);
    }

    /** Multiplier from where the player is. The Nether replaces the hot-biome multiplier instead of stacking with it. */
    public static float multiplier(boolean ultrawarm, float biomeTemperature, RustConfig.Thirst config) {
        if (ultrawarm) {
            return config.netherMultiplier;
        }
        if (biomeTemperature > config.hotBiomeTemperature) {
            return config.hotBiomeMultiplier;
        }
        return 1f;
    }

    /** Total drain for one tick: the capped base drain times the place multiplier. */
    public static float drainPerTick(boolean moving, boolean sprinting, boolean jumped,
                                     boolean ultrawarm, float biomeTemperature, RustConfig.Thirst config) {
        return baseDrain(moving, sprinting, jumped, config) * multiplier(ultrawarm, biomeTemperature, config);
    }

    /** Thirst needed to start sprinting: below 3 droplets (6 of 20) sprinting is blocked, like vanilla food at 6 or less. */
    public static final int MIN_THIRST_TO_SPRINT = 6;

    public static boolean canSprint(int thirst) {
        return thirst >= MIN_THIRST_TO_SPRINT;
    }

    /** Adds drain to the counter. When it reaches the limit, thirst drops by 1 (never below 0) and the counter resets. */
    public static RustPlayerData applyDrain(RustPlayerData data, float drain, int exhaustionLimit) {
        float exhaustion = data.thirstExhaustion() + drain;
        if (exhaustion >= exhaustionLimit) {
            return data.withThirst(data.thirst() - 1).withThirstExhaustion(0);
        }
        return data.withThirstExhaustion(exhaustion);
    }

    /**
     * Whether dehydration may hurt a player with this much health. Same rule as vanilla starvation:
     * never on Peaceful, down to 5 hearts on Easy, down to half a heart on Normal, to death on Hard.
     */
    public static boolean canDehydrationHurt(float health, Difficulty difficulty) {
        return switch (difficulty) {
            case PEACEFUL -> false;
            case EASY -> health > 10.0f;
            case NORMAL -> health > 1.0f;
            case HARD -> true;
        };
    }
}
