package net.rafa.rustcraft.util;

import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.rafa.rustcraft.RustCraft;

/** Tags defined by this mod. Their contents are generated in the datagen tag providers. */
public final class ModTags {
    private ModTags() {}

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
