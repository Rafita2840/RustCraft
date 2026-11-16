package net.rafa.rustcraft.block.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.rafa.rustcraft.data.PlayerStats;

/** Debug block: sneak + right-click to drink. Removed or made creative-only once real water exists (P1.2). */
public class DrinkableBlock extends Block {

    public DrinkableBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer && serverPlayer.isSneaking()) {
            PlayerStats.addThirst(serverPlayer, 1);
        }
        return ActionResult.SUCCESS;
    }
}
