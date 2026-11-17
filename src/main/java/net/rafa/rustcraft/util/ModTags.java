package net.rafa.rustcraft.util;

import net.minecraft.block.Block;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.rafa.rustcraft.RustCraft;

/** Tags defined by this mod. Their contents are generated in the datagen tag providers. */
public final class ModTags {
    private ModTags() {}

    /** Damage type tags. Only tags the mod adds its own types to are listed here. */
    public static final class DamageTypes {
        private DamageTypes() {}

        /** Vanilla tag {@code #minecraft:no_knockback}: damage of these types doesn't push the victim. */
        public static final TagKey<DamageType> NO_KNOCKBACK =
                TagKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of("minecraft", "no_knockback"));
    }

    public static final class Blocks {
        private Blocks() {}

        /** Every wood/stone/metal/HQM building block (centers included). */
        public static final TagKey<Block> BUILDING_BLOCKS = of("building_blocks");
        /** Only the "center" building blocks. */
        public static final TagKey<Block> CENTER_BUILDING_BLOCKS = of("center_building_blocks");
        /** Surfaces blueprints are allowed to build on. */
        public static final TagKey<Block> BUILDABLE_SURFACES = of("buildable_surfaces");

        private static TagKey<Block> of(String name) {
            return TagKey.of(RegistryKeys.BLOCK, Identifier.of(RustCraft.MOD_ID, name));
        }
    }
}
