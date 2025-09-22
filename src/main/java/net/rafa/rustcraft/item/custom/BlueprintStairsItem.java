package net.rafa.rustcraft.item.custom;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.rafa.rustcraft.block.ModBlocks;
import net.rafa.rustcraft.item.ModItems;

import java.util.Set;

public class BlueprintStairsItem extends Item {

    private static final Set<Block> CENTER_BUILDING_BLOCKS =
            Set.of(
                    ModBlocks.WOODEN_BUILDING_BLOCK_CENTER, ModBlocks.STONE_BUILDING_BLOCK_CENTER,
                    ModBlocks.METAL_BUILDING_BLOCK_CENTER, ModBlocks.HIGH_QUALITY_METAL_BUILDING_BLOCK_CENTER
            );


    private BlockPos[] stairsPlace;
    private boolean bellowFloor;

    public BlueprintStairsItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient){
            ServerPlayerEntity player = ((ServerPlayerEntity) user);
            if (player.isSneaking()){
                int pos = player.getInventory().getSlotWithStack(player.getStackInHand(hand));
                player.getInventory().removeStack(pos);
                player.getInventory().insertStack(pos, ModItems.BLUEPRINT_FLOOR.getDefaultStack());
            }
        }
        return super.use(world, user, hand);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        Block clickedBlock = world.getBlockState(context.getBlockPos()).getBlock();
        if (!world.isClient) {
            stairsPlace = new BlockPos[7];
            bellowFloor = CENTER_BUILDING_BLOCKS.contains(world.getBlockState(context.getBlockPos().up(4)).getBlock());
            Direction facing = context.getHorizontalPlayerFacing();
            ServerPlayerEntity player = ((ServerPlayerEntity) context.getPlayer());
            if (player != null) {
                if (CENTER_BUILDING_BLOCKS.contains(clickedBlock)) {
                    if (context.getSide().equals(Direction.UP)) {
                        if (checks(world, context, player, facing)) {
                            placeStairs(world, player, facing);
                        } else {
                            playSound(world, player, SoundEvents.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR);
                            player.sendMessage(Text.translatable("text.rustcraft.cant_place_stairs"), true);
                        }
                    } else {
                        playSound(world, player, SoundEvents.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR);
                        player.sendMessage(Text.translatable("text.rustcraft.cant_place_stairs"), true);
                    }
                } else {
                    playSound(world, player, SoundEvents.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR);
                    player.sendMessage(Text.translatable("text.rustcraft.cant_use"), true);
                }
            }
        }
        return ActionResult.SUCCESS;
    }

    private void placeStairs(World world, ServerPlayerEntity player, Direction facing) {
        if (!world.isClient){

        }
    }

    private boolean checks(World world, ItemUsageContext context, ServerPlayerEntity player, Direction facing) {
        boolean checks = false;
        BlockPos pos = context.getBlockPos();
        switch (facing){
            case EAST ->{
                stairsPlace[0] = pos.add(-1, 1, 1);
                stairsPlace[1] = pos.add(0, 2, 1);
                stairsPlace[2] = pos.add(1, 2, 1);
                stairsPlace[3] = pos.add(1, 2, 0);
                stairsPlace[4] = pos.add(1, 2, -1);
                stairsPlace[5] = pos.add(0, 3, -1);
                stairsPlace[6] = pos.add(-1, 4, -1);
            }
            case WEST -> {
                stairsPlace[0] = pos.add(1, 1, -1);
                stairsPlace[1] = pos.add(0, 2, -1);
                stairsPlace[2] = pos.add(-1, 2, -1);
                stairsPlace[3] = pos.add(-1, 2, 0);
                stairsPlace[4] = pos.add(-1, 2, 1);
                stairsPlace[5] = pos.add(0, 3, 1);
                stairsPlace[6] = pos.add(1, 4, 1);
            }
            case NORTH -> {
                stairsPlace[0] = pos.add(1, 1, 1);
                stairsPlace[1] = pos.add(1, 2, 0);
                stairsPlace[2] = pos.add(1, 2, -1);
                stairsPlace[3] = pos.add(0, 2, -1);
                stairsPlace[4] = pos.add(-1, 2, -1);
                stairsPlace[5] = pos.add(-1, 3, 0);
                stairsPlace[6] = pos.add(-1, 4, 1);
            }
            case SOUTH -> {
                stairsPlace[0] = pos.add(-1, 1, -1);
                stairsPlace[1] = pos.add(-1, 2, 0);
                stairsPlace[2] = pos.add(-1, 2, 1);
                stairsPlace[3] = pos.add(0, 2, 1);
                stairsPlace[4] = pos.add(1, 2, 1);
                stairsPlace[5] = pos.add(1, 3, 0);
                stairsPlace[6] = pos.add(1, 4, -1);
            }
        }
        int count = 0;
        for (int i = 0; i < stairsPlace.length; i++){
            if (!(bellowFloor && i == 6)){
                if (world.getBlockState(stairsPlace[i]).getBlock().equals(Blocks.AIR)){
                    count++;
                }
            }
        }
        checks = count == 7;
        if (bellowFloor && count == 6 && !world.getBlockState(stairsPlace[6]).getBlock().equals(Blocks.AIR))
            checks = true;
        return checks;
    }

    private void playSound(World world, PlayerEntity player, SoundEvent sound){
        world.playSound(null, player.getBlockPos(), sound, SoundCategory.BLOCKS, 20, 1);
    }
}
