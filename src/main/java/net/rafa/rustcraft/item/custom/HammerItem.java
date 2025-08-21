package net.rafa.rustcraft.item.custom;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.rafa.rustcraft.block.ModBlocks;
import net.rafa.rustcraft.item.ModItems;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class HammerItem extends Item {

    private static final Set<Block> CENTER_BUILDING_BLOCKS =
            Set.of(
                    ModBlocks.WOODEN_BUILDING_BLOCK_CENTER, ModBlocks.STONE_BUILDING_BLOCK_CENTER,
                    ModBlocks.METAL_BUILDING_BLOCK_CENTER, ModBlocks.HIGH_QUALITY_METAL_BUILDING_BLOCK_CENTER
            );

    private static final Set<Block> BUILDING_BLOCKS =
            Set.of(
                    ModBlocks.WOODEN_BUILDING_BLOCK_CENTER, ModBlocks.WOODEN_BUILDING_BLOCK,
                    ModBlocks.STONE_BUILDING_BLOCK_CENTER, ModBlocks.STONE_BUILDING_BLOCK,
                    ModBlocks.METAL_BUILDING_BLOCK_CENTER, ModBlocks.METAL_BUILDING_BLOCK,
                    ModBlocks.HIGH_QUALITY_METAL_BUILDING_BLOCK_CENTER, ModBlocks.HIGH_QUALITY_METAL_BUILDING_BLOCK
            );

    private static final Map<Block, Block> HAMMER_BLOCKS_CENTER_TO_OTHER =
            Map.of(
                    ModBlocks.WOODEN_BUILDING_BLOCK_CENTER, ModBlocks.WOODEN_BUILDING_BLOCK,
                    ModBlocks.STONE_BUILDING_BLOCK_CENTER, ModBlocks.STONE_BUILDING_BLOCK,
                    ModBlocks.METAL_BUILDING_BLOCK_CENTER, ModBlocks.METAL_BUILDING_BLOCK,
                    ModBlocks.HIGH_QUALITY_METAL_BUILDING_BLOCK_CENTER, ModBlocks.HIGH_QUALITY_METAL_BUILDING_BLOCK
            );

    private static final Map<Block, Block> HAMMER_BLOCKS =
            Map.of(
                    ModBlocks.WOODEN_BUILDING_BLOCK_CENTER, ModBlocks.STONE_BUILDING_BLOCK,
                    ModBlocks.STONE_BUILDING_BLOCK_CENTER, ModBlocks.METAL_BUILDING_BLOCK,
                    ModBlocks.METAL_BUILDING_BLOCK_CENTER, ModBlocks.HIGH_QUALITY_METAL_BUILDING_BLOCK
            );

    private static final Map<Block, Block> HAMMER_CENTER_BLOCK =
            Map.of(
                    ModBlocks.WOODEN_BUILDING_BLOCK_CENTER, ModBlocks.STONE_BUILDING_BLOCK_CENTER,
                    ModBlocks.STONE_BUILDING_BLOCK_CENTER, ModBlocks.METAL_BUILDING_BLOCK_CENTER,
                    ModBlocks.METAL_BUILDING_BLOCK_CENTER, ModBlocks.HIGH_QUALITY_METAL_BUILDING_BLOCK_CENTER
            );

    private static final Map<Block, Item> HAMMER_ITEM_MAP =
            Map.of(
                    ModBlocks.WOODEN_BUILDING_BLOCK_CENTER, ModItems.STONE,
                    ModBlocks.STONE_BUILDING_BLOCK_CENTER, ModItems.METAL,
                    ModBlocks.METAL_BUILDING_BLOCK_CENTER, ModItems.HIGH_QUALITY_METAL
            );

    private static final Map<Block, SoundEvent> HAMMER_SOUND_MAP =
            Map.of(
                    ModBlocks.WOODEN_BUILDING_BLOCK_CENTER, SoundEvents.ENTITY_IRON_GOLEM_REPAIR,
                    ModBlocks.STONE_BUILDING_BLOCK_CENTER, SoundEvents.BLOCK_SMITHING_TABLE_USE,
                    ModBlocks.METAL_BUILDING_BLOCK_CENTER, SoundEvents.BLOCK_ANVIL_USE
            );

    public HammerItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        Block clickedBlock = world.getBlockState(context.getBlockPos()).getBlock();
        Direction side = context.getSide();
        if (!world.isClient) {
            ServerPlayerEntity player = ((ServerPlayerEntity) context.getPlayer());
            if (player != null) {
                if (player.isSneaking()) {
                    if (clickedBlock.equals(ModBlocks.WOODEN_BUILDING_BLOCK_CENTER)) {
                        if (side.equals(Direction.DOWN) || side.equals(Direction.UP))
                            removeFloor(world, context, player);
                        else
                            removeWall(world, context, clickedBlock, player, side);
                    } else {
                        player.playSoundToPlayer(SoundEvents.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, SoundCategory.BLOCKS, 1, 1);
                        player.sendMessage(Text.translatable("text.rustcraft.cant_remove"), true);
                    }
                } else if (HAMMER_CENTER_BLOCK.containsKey(clickedBlock)) {
                    if (side.equals(Direction.EAST) || side.equals(Direction.WEST)
                            || side.equals(Direction.NORTH) || side.equals(Direction.SOUTH))
                        upgradeWall(world, context, clickedBlock, player);
                    else
                        upgradeFloor(world, context, clickedBlock, player);
                } else {
                    player.playSoundToPlayer(SoundEvents.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, SoundCategory.BLOCKS, 1, 1);
                    player.sendMessage(Text.translatable("text.rustcraft.cant_use"), true);
                }
            }
        }
        return ActionResult.SUCCESS;
    }

    private void upgradeWall(World world, ItemUsageContext context, Block clickedBlock, ServerPlayerEntity player) {
        if (!world.isClient) {
            int amount = 12;
            if (player != null) {
                int[][] posAndAmount = new int[9][2];
                int k = 0;
                int count = 0;
                boolean success = false;
                while (!success && k < 9) {
                    posAndAmount[k][0] = player.getInventory().getSlotWithStack(HAMMER_ITEM_MAP.get(clickedBlock).getDefaultStack());
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
            }
        }
    }

    private void upgradeFloor(World world, ItemUsageContext context, Block clickedBlock, ServerPlayerEntity player) {
        if (!world.isClient) {
            int amount = 1;
            for (int i = -1; i < 2; i++){
                for (int j = -1; j < 2; j++){
                    if (world.getBlockState(context.getBlockPos().add(i,0,j)).equals(HAMMER_BLOCKS_CENTER_TO_OTHER.get(clickedBlock).getDefaultState()))
                        amount++;
                }
            }
            if (player != null) {
                int[][] posAndAmount = new int[amount][2];
                int k = 0;
                int count = 0;
                boolean success = false;
                while (!success && k < amount) {
                    posAndAmount[k][0] = player.getInventory().getSlotWithStack(HAMMER_ITEM_MAP.get(clickedBlock).getDefaultStack());
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
                    if (k==0)
                        player.getInventory().removeStack(posAndAmount[k][0], amount);
                    for (int i = -1; i < 2; i++) {
                        for (int j = -1; j < 2; j++) {
                            if (i != 0 || j != 0)
                                if (world.getBlockState(context.getBlockPos().add(i,0,j)).equals(HAMMER_BLOCKS_CENTER_TO_OTHER.get(clickedBlock).getDefaultState()))
                                    world.setBlockState(context.getBlockPos().add(i, 0, j), HAMMER_BLOCKS.get(clickedBlock).getDefaultState());
                        }
                    }
                    world.setBlockState(context.getBlockPos(), HAMMER_CENTER_BLOCK.get(clickedBlock).getDefaultState());
                    player.playSoundToPlayer(HAMMER_SOUND_MAP.get(clickedBlock), SoundCategory.BLOCKS, 20, 1);
                } else  {
                    player.playSoundToPlayer(SoundEvents.UI_STONECUTTER_TAKE_RESULT, SoundCategory.BLOCKS, 20, 1);
                }
                if (k > 0 && !success){
                    for (int n = 0; n < k; n++){
                        for (int l = 0; l < posAndAmount[n][1]; l++)
                            player.getInventory().insertStack(posAndAmount[n][0], HAMMER_ITEM_MAP.get(clickedBlock).getDefaultStack());
                    }
                }
                if (k > 0 && success){
                    for (int p = 0; p < count - amount; p++)
                        player.getInventory().insertStack(posAndAmount[k][0], HAMMER_ITEM_MAP.get(clickedBlock).getDefaultStack());

                }
            }
        }
    }

    private void removeFloor(World world, ItemUsageContext context, ServerPlayerEntity player) {
        int removed = 0;
        int initI = -1;
        int initJ = -1;
        int endI = 2;
        int endJ = 2;
        if (CENTER_BUILDING_BLOCKS.contains(world.getBlockState((context.getBlockPos().add(-1, 2, 0))).getBlock()))
            initI = 0;
        if (CENTER_BUILDING_BLOCKS.contains(world.getBlockState((context.getBlockPos().add(0, 2, -1))).getBlock()))
            initJ = 0;
        if (CENTER_BUILDING_BLOCKS.contains(world.getBlockState((context.getBlockPos().add(1, 2, 0))).getBlock()))
            endI = 1;
        if (CENTER_BUILDING_BLOCKS.contains(world.getBlockState((context.getBlockPos().add(0, 2, 1))).getBlock()))
            endJ = 1;
        for (int i = initI; i < endI; i++) {
            for (int j = initJ; j < endJ; j++) {
                if (world.getBlockState((context.getBlockPos().add(i, 0, j))).equals(ModBlocks.WOODEN_BUILDING_BLOCK_CENTER.getDefaultState())
                        || world.getBlockState((context.getBlockPos().add(i, 0, j))).equals(ModBlocks.WOODEN_BUILDING_BLOCK.getDefaultState())) {
                    world.setBlockState(context.getBlockPos().add(i, 0, j), Blocks.AIR.getDefaultState());
                    removed++;
                }
            }
        }
        for (int n = 0; n < removed; n++)
            player.getInventory().offerOrDrop(ModItems.WOOD.getDefaultStack());
        player.playSoundToPlayer(SoundEvents.BLOCK_LADDER_STEP, SoundCategory.BLOCKS, 20, 1);
    }

    private void removeWall(World world, ItemUsageContext context, Block clickedBlock, ServerPlayerEntity player, Direction side) {
        if (!world.isClient) {
            int removed = 0;
            boolean keepFloor = isOverFloor(world, context, side);
            boolean keepWallLeft = isOverWall(world, context, true, side);
            boolean keepWallRight = isOverWall(world, context, false, side);
            if (side.equals(Direction.EAST) || side.equals(Direction.WEST)) {
                for (int y = -2; y < 2; y++) {
                    for (int z = -1; z < 2; z++) {
                        if (!(keepFloor && y == -2)) {
                            if (!(keepWallLeft && z == 1) && !(keepWallRight && z == -1)) {
                                if (world.getBlockState(context.getBlockPos().add(0, y, z)).equals(ModBlocks.WOODEN_BUILDING_BLOCK.getDefaultState())
                                        || world.getBlockState(context.getBlockPos().add(0, y, z)).equals(ModBlocks.WOODEN_BUILDING_BLOCK_CENTER.getDefaultState())) {
                                    world.setBlockState(context.getBlockPos().add(0, y, z), Blocks.AIR.getDefaultState());
                                    world.addBlockBreakParticles(context.getBlockPos().add(0, y, z), ModBlocks.WOODEN_BUILDING_BLOCK.getDefaultState());
                                    removed++;
                                }
                            }
                        }
                    }
                }
            } else {
                for (int y = -2; y < 2; y++) {
                    for (int x = -1; x < 2; x++) {
                        if (!(keepFloor && y == -2)) {
                            if (!(keepWallLeft && x == -1) && !(keepWallRight && x == 1)) {
                                if (world.getBlockState(context.getBlockPos().add(x, y, 0)).equals(ModBlocks.WOODEN_BUILDING_BLOCK.getDefaultState())
                                        || world.getBlockState(context.getBlockPos().add(x, y, 0)).equals(ModBlocks.WOODEN_BUILDING_BLOCK_CENTER.getDefaultState())) {
                                    world.setBlockState(context.getBlockPos().add(x, y, 0), Blocks.AIR.getDefaultState());
                                    world.addBlockBreakParticles(context.getBlockPos().add(x, y, 0), ModBlocks.WOODEN_BUILDING_BLOCK.getDefaultState());
                                    removed++;
                                }
                            }
                        }
                    }
                }
            }
            for (int i = 0; i < removed; i++)
                player.getInventory().offerOrDrop(ModItems.WOOD.getDefaultStack());
            player.playSoundToPlayer(SoundEvents.BLOCK_LADDER_STEP, SoundCategory.BLOCKS, 20, 1);
        }
    }

    private boolean isOverWall(World world, ItemUsageContext context, boolean side, Direction direction) {
        if (!world.isClient) {
            boolean dir = direction.equals(Direction.NORTH) || direction.equals(Direction.SOUTH);
            if (side) {
                if (dir) {
                    return CENTER_BUILDING_BLOCKS.contains(world.getBlockState(context.getBlockPos().add(-1, 0, -1)).getBlock())
                            || CENTER_BUILDING_BLOCKS.contains(world.getBlockState(context.getBlockPos().add(-1, 0, 1)).getBlock());
                } else
                    return CENTER_BUILDING_BLOCKS.contains(world.getBlockState(context.getBlockPos().add(1, 0, 1)).getBlock())
                            || CENTER_BUILDING_BLOCKS.contains(world.getBlockState(context.getBlockPos().add(-1, 0, 1)).getBlock());
            } else {
                if (dir) {
                    return CENTER_BUILDING_BLOCKS.contains(world.getBlockState(context.getBlockPos().add(1, 0, -1)).getBlock())
                            || CENTER_BUILDING_BLOCKS.contains(world.getBlockState(context.getBlockPos().add(1, 0, 1)).getBlock());
                } else
                    return CENTER_BUILDING_BLOCKS.contains(world.getBlockState(context.getBlockPos().add(1, 0, -1)).getBlock())
                            || CENTER_BUILDING_BLOCKS.contains(world.getBlockState(context.getBlockPos().add(-1, 0, -1)).getBlock());
            }
        }
        return false;
    }

    private boolean isOverFloor(World world, ItemUsageContext context, Direction side) {
        if (!world.isClient) {
            if (side.equals(Direction.EAST) || side.equals(Direction.WEST)) {
                return CENTER_BUILDING_BLOCKS.contains(world.getBlockState(context.getBlockPos().add(1, -2, 0)).getBlock())
                        || CENTER_BUILDING_BLOCKS.contains(world.getBlockState(context.getBlockPos().add(-1, -2, 0)).getBlock());
            } else {
                return CENTER_BUILDING_BLOCKS.contains(world.getBlockState(context.getBlockPos().add(0, -2, 1)).getBlock())
                        || CENTER_BUILDING_BLOCKS.contains(world.getBlockState(context.getBlockPos().add(0, -2, -1)).getBlock());
            }
        }
        return false;
    }
}
