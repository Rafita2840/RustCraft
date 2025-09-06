package net.rafa.rustcraft.block.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.rafa.rustcraft.util.IEntityDataSaver;
import net.rafa.rustcraft.util.ThirstData;

public class DrinkableBlock extends Block {

    public DrinkableBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            ServerPlayerEntity serverPlayer = ((ServerPlayerEntity) player);
            if (serverPlayer.isSneaking()) {
                ThirstData.addThirst(((IEntityDataSaver) player), 1);
            }
            int thirst = ((IEntityDataSaver) player).getPersistentData().getInt("thirst");
//            serverPlayer.sendMessage(Text.literal("Thirst " + thirst)
//                            .fillStyle(Style.EMPTY
//                                    .withColor(Formatting.AQUA)),
//                    false);
            ThirstData.syncThirst(thirst, ((IEntityDataSaver) player));
        }
        return ActionResult.SUCCESS;
    }
}
