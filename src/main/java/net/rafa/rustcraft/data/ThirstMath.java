package net.rafa.rustcraft.data;

import net.rafa.rustcraft.config.RustConfig;

/** The thirst rules as plain functions (no world or player needed), so they can be tested on their own. */
public final class ThirstMath {
    private ThirstMath() {}

    /**
     * Drain added to the hidden counter in one tick.
     *
     * @param speedVariation horizontal distance moved this tick (walking is above 0, running above 0.13, sprinting above 0.20)
     */
    public static int drainPerTick(float speedVariation, RustConfig.Thirst config) {
        int amount = config.idleDrain;
        if (speedVariation > 0f) {
            amount += config.walkDrain;
        }
        if (speedVariation > 0.13f) {
            amount += config.runDrain;
        }
        if (speedVariation > 0.20f) {
            amount += config.sprintDrain;
        }
        return amount;
    }

    /** Adds drain to the counter. When it reaches the limit, thirst drops by 1 (never below 0) and the counter resets. */
    public static RustPlayerData applyDrain(RustPlayerData data, int drain, int exhaustionLimit) {
        int exhaustion = data.thirstExhaustion() + drain;
        if (exhaustion >= exhaustionLimit) {
            return data.withThirst(data.thirst() - 1).withThirstExhaustion(0);
        }
        return data.withThirstExhaustion(exhaustion);
    }
}
