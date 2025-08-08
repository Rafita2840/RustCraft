package net.rafa.rustcraft.item.custom;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.rafa.rustcraft.item.ModItems;

public class BlueprintStairsItem extends Item {

    public BlueprintStairsItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient){
            ServerPlayerEntity player = ((ServerPlayerEntity) user);
            if (player.isInSneakingPose()){
                int pos = player.getInventory().getSlotWithStack(player.getStackInHand(hand));
                player.getInventory().removeStack(pos);
                player.getInventory().insertStack(pos, ModItems.BLUEPRINT_FLOOR.getDefaultStack());
            }
        }
        return super.use(world, user, hand);
    }
}
