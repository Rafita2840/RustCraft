package net.rafa.rustcraft.util;

import java.util.function.BooleanSupplier;

/**
 * Lets common code (item tooltips) ask whether Shift / Control are held
 * without importing any client class. The client sets the real providers in
 * RustCraftClient; on a dedicated server both always return false.
 */
public final class TooltipKeys {
    private static BooleanSupplier shift = () -> false;
    private static BooleanSupplier control = () -> false;

    private TooltipKeys() {}

    public static void setProviders(BooleanSupplier shiftProvider, BooleanSupplier controlProvider) {
        shift = shiftProvider;
        control = controlProvider;
    }

    public static boolean shiftDown() { return shift.getAsBoolean(); }

    public static boolean controlDown() { return control.getAsBoolean(); }
}
