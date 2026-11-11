package net.rafa.rustcraft.util;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Builds coloured Text from plain lang keys, so lang files contain no legacy colour codes. */
public final class ModText {
    private ModText() {}

    /** Red error / status message. */
    public static Text error(String key) {
        return Text.translatable(key).formatted(Formatting.RED);
    }

    /** Dark aqua tooltip heading line. */
    public static Text heading(String key) {
        return Text.translatable(key).formatted(Formatting.DARK_AQUA);
    }

    /** Gray tooltip description line. */
    public static Text info(String key) {
        return Text.translatable(key).formatted(Formatting.GRAY);
    }

    /** "Hold Shift for information" (Shift is white while it is being held). */
    public static Text holdShift(boolean holding) {
        return Text.translatable("tooltip.rustcraft.tool.hold_shift",
                Text.literal("Shift").formatted(holding ? Formatting.WHITE : Formatting.YELLOW))
                .formatted(Formatting.GOLD);
    }

    /** "Hold Control for controls" (Control is white while it is being held). */
    public static Text holdControl(boolean holding) {
        return Text.translatable("tooltip.rustcraft.tool.hold_control",
                Text.literal("Control").formatted(holding ? Formatting.WHITE : Formatting.AQUA))
                .formatted(Formatting.GOLD);
    }
}
