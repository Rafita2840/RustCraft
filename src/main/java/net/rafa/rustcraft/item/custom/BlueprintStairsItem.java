package net.rafa.rustcraft.item.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
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

    private static final Item RESOURCE_NEEDED = ModItems.WOOD;

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
        int amount = bellowFloor ? 6 : 7;
            if (player != null) {
                int[][] posAndAmount = new int[amount][2];
                int k = 0;
                int count = 0;
                boolean success = false;
                while (!success && k < amount) {
                    posAndAmount[k][0] = player.getInventory().getSlotWithStack(RESOURCE_NEEDED.getDefaultStack());
                    if (posAndAmount[k][0] == -1)
                        break;
                    posAndAmount[k][1] = player.getInventory().getStack(posAndAmount[k][0]).getCount();
                    count += posAndAmount[k][1];
                    if (count < amount) {
                        player.getInventory().removeStack(posAndAmount[k][0], posAndAmount[k][1]);
                        k++;
                    } else {
                        if (k > 0)
                            player.getInventory().removeStack(posAndAmount[k][0], posAndAmount[k][1]);
                        success = true;
                    }
                }
                if (success || k == amount - 1) {
                    if (k == 0)
                        player.getInventory().removeStack(posAndAmount[k][0], amount);
                    switch (facing){
                        case NORTH -> {
                            world.setBlockState(stairsPlace[0], Blocks.OAK_STAIRS.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH));
                            world.setBlockState(stairsPlace[1], Blocks.OAK_STAIRS.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH));
                            world.setBlockState(stairsPlace[2], Blocks.OAK_SLAB.getDefaultState().with(Properties.SLAB_TYPE, SlabType.TOP));
                            world.setBlockState(stairsPlace[3], Blocks.OAK_SLAB.getDefaultState().with(Properties.SLAB_TYPE, SlabType.TOP));
                            world.setBlockState(stairsPlace[4], Blocks.OAK_SLAB.getDefaultState().with(Properties.SLAB_TYPE, SlabType.TOP));
                            world.setBlockState(stairsPlace[5], Blocks.OAK_STAIRS.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.SOUTH));
                            if (!bellowFloor)
                                world.setBlockState(stairsPlace[6], Blocks.OAK_STAIRS.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.SOUTH));
                        }
                        case SOUTH -> {
                            world.setBlockState(stairsPlace[0], Blocks.OAK_STAIRS.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.SOUTH));
                            world.setBlockState(stairsPlace[1], Blocks.OAK_STAIRS.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.SOUTH));
                            world.setBlockState(stairsPlace[2], Blocks.OAK_SLAB.getDefaultState().with(Properties.SLAB_TYPE, SlabType.TOP));
                            world.setBlockState(stairsPlace[3], Blocks.OAK_SLAB.getDefaultState().with(Properties.SLAB_TYPE, SlabType.TOP));
                            world.setBlockState(stairsPlace[4], Blocks.OAK_SLAB.getDefaultState().with(Properties.SLAB_TYPE, SlabType.TOP));
                            world.setBlockState(stairsPlace[5], Blocks.OAK_STAIRS.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH));
                            if (!bellowFloor)
                                world.setBlockState(stairsPlace[6], Blocks.OAK_STAIRS.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH));
                        }
                        case EAST -> {
                            world.setBlockState(stairsPlace[0], Blocks.OAK_STAIRS.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.EAST));
                            world.setBlockState(stairsPlace[1], Blocks.OAK_STAIRS.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.EAST));
                            world.setBlockState(stairsPlace[2], Blocks.OAK_SLAB.getDefaultState().with(Properties.SLAB_TYPE, SlabType.TOP));
                            world.setBlockState(stairsPlace[3], Blocks.OAK_SLAB.getDefaultState().with(Properties.SLAB_TYPE, SlabType.TOP));
                            world.setBlockState(stairsPlace[4], Blocks.OAK_SLAB.getDefaultState().with(Properties.SLAB_TYPE, SlabType.TOP));
                            world.setBlockState(stairsPlace[5], Blocks.OAK_STAIRS.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.WEST));
                            if (!bellowFloor)
                                world.setBlockState(stairsPlace[6], Blocks.OAK_STAIRS.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.WEST));
                        }
                        case WEST -> {
                            world.setBlockState(stairsPlace[0], Blocks.OAK_STAIRS.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.WEST));
                            world.setBlockState(stairsPlace[1], Blocks.OAK_STAIRS.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.WEST));
                            world.setBlockState(stairsPlace[2], Blocks.OAK_SLAB.getDefaultState().with(Properties.SLAB_TYPE, SlabType.TOP));
                            world.setBlockState(stairsPlace[3], Blocks.OAK_SLAB.getDefaultState().with(Properties.SLAB_TYPE, SlabType.TOP));
                            world.setBlockState(stairsPlace[4], Blocks.OAK_SLAB.getDefaultState().with(Properties.SLAB_TYPE, SlabType.TOP));
                            world.setBlockState(stairsPlace[5], Blocks.OAK_STAIRS.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.EAST));
                            if (!bellowFloor)
                                world.setBlockState(stairsPlace[6], Blocks.OAK_STAIRS.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.EAST));
                        }
                    }
                    playSound(world, player, SoundEvents.ENTITY_VILLAGER_WORK_FLETCHER);
                }
                else  {
                    playSound(world, player, SoundEvents.UI_STONECUTTER_TAKE_RESULT);
                    player.sendMessage(Text.translatable("text.rustcraft.not_enough_resources"), true);
                }
                if (k > 0 && !success){
                    for (int n = 0; n < k; n++){
                        for (int l = 0; l < posAndAmount[n][1]; l++)
                            player.getInventory().insertStack(posAndAmount[n][0], RESOURCE_NEEDED.getDefaultStack());
                    }
                }
                if (k > 0 && success && k < amount - 1){
                    for (int p = 0; p < count - amount; p++)
                        player.getInventory().insertStack(posAndAmount[k][0], RESOURCE_NEEDED.getDefaultStack());

                }
            }
        }
    }

    private boolean checks(World world, ItemUsageContext context, ServerPlayerEntity player, Direction facing) {
        boolean checks;
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
