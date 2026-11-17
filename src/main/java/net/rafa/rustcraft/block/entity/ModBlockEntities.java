package net.rafa.rustcraft.block.entity;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.rafa.rustcraft.RustCraft;
import net.rafa.rustcraft.block.ModBlocks;
import net.rafa.rustcraft.block.entity.custom.CrateBlockEntity;
import net.rafa.rustcraft.block.entity.custom.VendingMachineBlockEntity;

/** Block entity types. Each one uses the same id as its block. */
public final class ModBlockEntities {
    private ModBlockEntities() {}

    public static final BlockEntityType<VendingMachineBlockEntity> VENDING_MACHINE = Registry.register(
            Registries.BLOCK_ENTITY_TYPE, Identifier.of(RustCraft.MOD_ID, "vending_machine"),
            BlockEntityType.Builder.create(VendingMachineBlockEntity::new, ModBlocks.VENDING_MACHINE).build(null));

    public static final BlockEntityType<CrateBlockEntity> CRATE = Registry.register(
            Registries.BLOCK_ENTITY_TYPE, Identifier.of(RustCraft.MOD_ID, "crate"),
            BlockEntityType.Builder.create(CrateBlockEntity::new, ModBlocks.CRATE).build(null));

    public static void registerModBlockEntities() {
        RustCraft.LOGGER.info("Registering Mod BlockEntities for " + RustCraft.MOD_ID);
    }
}
