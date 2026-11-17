package net.rafa.rustcraft.util;

/**
 * Where the HUD rows go, as plain functions so they can be tested without a client. The numbers mirror what
 * vanilla {@code InGameHud.renderStatusBars} does (checked against 1.21.1): the food row is 39 pixels above the
 * bottom of the screen, the air bubbles are one row (10 px) above it, and a mount's hearts replace the food bar
 * and push the bubbles up by one row per extra 10 hearts. The thirst bar takes the bubbles' row.
 */
public final class HudLayout {
    private HudLayout() {}

    public static final int ROW_HEIGHT = 10;

    /** Hearts of the mount the player rides, as vanilla counts them: max health / 2, at most 30; 0 when not riding. */
    public static int mountHeartCount(boolean ridingLivingMount, float mountMaxHealth) {
        if (!ridingLivingMount) {
            return 0;
        }
        return Math.min(30, (int) (mountMaxHealth + 0.5f) / 2);
    }

    /**
     * How many pixels the thirst bar moves up from its normal row (the row above the food bar). The bar keeps that
     * row, and only goes up for every extra row of mount hearts, which take the same space. The air bubbles are the
     * ones that make room, see {@link #AIR_BUBBLES_RAISE}.
     */
    public static int thirstBarRaise(int mountHeartCount) {
        int mountRows = (int) Math.ceil(mountHeartCount / 10.0);
        return Math.max(0, mountRows - 1) * ROW_HEIGHT;
    }

    /** The air bubbles go one row up, above the thirst bar, while the thirst bar is shown. */
    public static final int AIR_BUBBLES_RAISE = ROW_HEIGHT + 1;
}
