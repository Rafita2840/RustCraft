package net.rafa.rustcraft.item.custom;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
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

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class BlueprintWallItem extends Item {

    private static final Map<Block, Block> CENTER_BLOCK_MAP =
            Map.of(
                    ModBlocks.WOODEN_BUILDING_BLOCK_CENTER, ModBlocks.WOODEN_BUILDING_BLOCK_CENTER,
                    ModBlocks.STONE_BUILDING_BLOCK_CENTER, ModBlocks.STONE_BUILDING_BLOCK_CENTER
            );

    private static final Set<Block> BUILDING_BLOCKS =
            Set.of(
                    ModBlocks.WOODEN_BUILDING_BLOCK, ModBlocks.STONE_BUILDING_BLOCK
            );
    private static final Item RESOURCE_NEEDED = ModItems.WOOD;

    private boolean overFloor;
    private boolean overWallLeft;
    private boolean overWallRight;
    private final BlockPos[] wallPlace;

    public BlueprintWallItem(Settings settings) {
        super(settings);
        overFloor = false;
        overWallLeft = false;
        overWallRight = false;
        wallPlace = new BlockPos[12];
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        overFloor = false;
        overWallLeft = false;
        overWallRight = false;
        if (!world.isClient){
            ServerPlayerEntity player = ((ServerPlayerEntity) user);
            if (player.isSneaking()){
                int pos = player.getInventory().getSlotWithStack(player.getStackInHand(hand));
                player.getInventory().removeStack(pos);
                player.getInventory().insertStack(pos, ModItems.BLUEPRINT_STAIRS.getDefaultStack());
            }
        }
        return super.use(world, user, hand);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        overFloor = false;
        overWallLeft = false;
        overWallRight = false;
        World world = context.getWorld();
        Block clickedBlock = world.getBlockState(context.getBlockPos()).getBlock();
        if (!world.isClient) {
            ServerPlayerEntity player = ((ServerPlayerEntity) context.getPlayer());
            if (player != null) {
                if (checks(world, context, clickedBlock, player)) {
                    if (CENTER_BLOCK_MAP.containsKey(clickedBlock) && context.getSide().equals(Direction.UP)) {
                        placeWall(world, player);
                    } else {
                        player.playSoundToPlayer(SoundEvents.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, SoundCategory.BLOCKS, 1, 1);
                    }
                } else {
                    player.playSoundToPlayer(SoundEvents.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, SoundCategory.BLOCKS, 1, 1);
                    player.sendMessage(Text.translatable("text.rustcraft.cant_use"), true);
                }
            }
        }
        return ActionResult.SUCCESS;
    }

    private boolean checks(World world, ItemUsageContext context, Block clickedBlock, ServerPlayerEntity player) {
        boolean checks = false;
        Direction direction = player.getHorizontalFacing();
        int xOffset = 0;
        int zOffset = 0;
        switch (direction) {
            case NORTH -> zOffset = -2;
            case SOUTH -> zOffset = 2;
            case WEST -> xOffset = -2;
            case EAST -> xOffset = 2;
        }
        wallPlace[0] = context.getBlockPos().add(xOffset, 0, zOffset);
        wallPlace[1] = context.getBlockPos().add(xOffset, 1, zOffset);
        wallPlace[2] = context.getBlockPos().add(xOffset, 2, zOffset);
        wallPlace[3] = context.getBlockPos().add(xOffset, 3, zOffset);
        if (xOffset != 0){
            wallPlace[4] = context.getBlockPos().add(xOffset, 0, 1);
            wallPlace[5] = context.getBlockPos().add(xOffset, 1, 1);
            wallPlace[6] = context.getBlockPos().add(xOffset, 2, 1);
            wallPlace[7] = context.getBlockPos().add(xOffset, 3, 1);
            wallPlace[8] = context.getBlockPos().add(xOffset, 0, -1);
            wallPlace[9] = context.getBlockPos().add(xOffset, 1, -1);
            wallPlace[10] = context.getBlockPos().add(xOffset, 2, -1);
            wallPlace[11] = context.getBlockPos().add(xOffset, 3, -1);
        } else {
            wallPlace[4] = context.getBlockPos().add(1, 0, zOffset);
            wallPlace[5] = context.getBlockPos().add(1, 1, zOffset);
            wallPlace[6] = context.getBlockPos().add(1, 2, zOffset);
            wallPlace[7] = context.getBlockPos().add(1, 3, zOffset);
            wallPlace[8] = context.getBlockPos().add(-1, 0, zOffset);
            wallPlace[9] = context.getBlockPos().add(-1, 1, zOffset);
            wallPlace[10] = context.getBlockPos().add(-1, 2, zOffset);
            wallPlace[11] = context.getBlockPos().add(-1, 3, zOffset);
        }
        int counter = 0;
        int floors = 0;
        int side1 = 0;
        int side2 = 0;
        int i = 0;
        while (!checks) {
            if (world.getBlockState(wallPlace[i]).getBlock().equals(Blocks.AIR))
                counter++;
            else if (i == 0 || i == 4 || i == 8)
                if (BUILDING_BLOCKS.contains(world.getBlockState(wallPlace[i]).getBlock())) {
                    floors++;
                    if (floors == 3)
                        overFloor = true;
                    counter++;
                    if (i == 4)
                        side1++;
                    if (i == 8)
                        side2++;
                }
            if (i == 5 || i == 6 || i == 7){
                if (BUILDING_BLOCKS.contains(world.getBlockState(wallPlace[i]).getBlock())) {
                    side1++;
                    if (side1 == 4)
                        overWallLeft = true;
                    counter++;
                }
            }
            if (i == 9 || i == 10 || i == 11){
                if (BUILDING_BLOCKS.contains(world.getBlockState(wallPlace[i]).getBlock())) {
                    side2++;
                    if (side2 == 4)
                        overWallRight = true;
                    counter++;
                }
            }
            i++;
            if (counter == 12)
                checks = true;
            if (i == 12)
                break;
        }
        return checks;
    }

    private void placeWall(World world, ServerPlayerEntity player) {
        Map<Integer, Integer> placeOn = new TreeMap<>();
        for (int i = 0; i < 12; i++){
            placeOn.put(i, i);
        }
        int amount = 12;
        if (overFloor && !(overWallLeft || overWallRight)) {
            amount = 9;
            placeOn.remove(0);
            placeOn.remove(4);
            placeOn.remove(8);
        }
        if (overWallRight && !overFloor) {
            amount = 8;
            placeOn.remove(8);
            placeOn.remove(9);
            placeOn.remove(10);
            placeOn.remove(11);
        }
        if (overWallRight && overFloor) {
            amount = 6;
            placeOn.remove(0);
            placeOn.remove(4);
            placeOn.remove(8);
            placeOn.remove(9);
            placeOn.remove(10);
            placeOn.remove(11);
        }
        if (overWallLeft && !overFloor) {
            amount = 8;
            placeOn.remove(4);
            placeOn.remove(5);
            placeOn.remove(6);
            placeOn.remove(7);
        }
        if (overWallLeft && overFloor) {
            amount = 6;
            placeOn.remove(0);
            placeOn.remove(4);
            placeOn.remove(5);
            placeOn.remove(6);
            placeOn.remove(7);
            placeOn.remove(8);
        }
        if (overWallRight && overWallLeft && overFloor) {
            amount = 3;
            placeOn.remove(0);
            placeOn.remove(4);
            placeOn.remove(5);
            placeOn.remove(6);
            placeOn.remove(7);
            placeOn.remove(8);
            placeOn.remove(9);
            placeOn.remove(10);
            placeOn.remove(11);
        }
        if (overWallRight && overWallLeft && !overFloor) {
            amount = 4;
            placeOn.remove(4);
            placeOn.remove(5);
            placeOn.remove(6);
            placeOn.remove(7);
            placeOn.remove(8);
            placeOn.remove(9);
            placeOn.remove(10);
            placeOn.remove(11);
            placeOn.remove(12);
        }


        if (player != null) {
            int[][] posAndAmount = new int[12][2];
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
            if (success || k == 8) {
                if (k == 0)
                    player.getInventory().removeStack(posAndAmount[k][0], amount);
                for (int i = 0; i < 12; i++) {
                    if (i != 2)
                        if (placeOn.containsKey(i))
                            world.setBlockState(wallPlace[i], ModBlocks.WOODEN_BUILDING_BLOCK.getDefaultState());
                }
                world.setBlockState(wallPlace[2], ModBlocks.WOODEN_BUILDING_BLOCK_CENTER.getDefaultState());
                player.playSoundToPlayer(SoundEvents.ENTITY_VILLAGER_WORK_FLETCHER, SoundCategory.BLOCKS, 20, 1);
            }
            else  {
                player.playSoundToPlayer(SoundEvents.UI_STONECUTTER_TAKE_RESULT, SoundCategory.BLOCKS, 20, 1);
            }
            if (k > 0 && !success){
                for (int n = 0; n < k; n++){
                    for (int l = 0; l < posAndAmount[n][1]; l++)
                        player.getInventory().insertStack(posAndAmount[n][0], RESOURCE_NEEDED.getDefaultStack());
                }
            }
            if (k > 0 && success){
                for (int p = 0; p < count - amount; p++)
                    player.getInventory().insertStack(posAndAmount[k][0], RESOURCE_NEEDED.getDefaultStack());

            }
        }
    }
}
