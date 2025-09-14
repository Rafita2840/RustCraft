package net.rafa.rustcraft.block.entity;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.rafa.rustcraft.RustCraft;
import net.rafa.rustcraft.block.ModBlocks;
import net.rafa.rustcraft.block.entity.custom.CrateBlockEntity;

public class ModBlockEntities {

    public static final BlockEntityType<CrateBlockEntity> CRATE_BE =
            Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(RustCraft.MOD_ID, "crate_be"),
                    BlockEntityType.Builder.create(CrateBlockEntity::new, ModBlocks.CRATE).build(null));

    public static void registerModBlockEntities(){
        RustCraft.LOGGER.info("Registering Mod BlockEntities for " + RustCraft.MOD_ID);
    }

}



