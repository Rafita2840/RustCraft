package net.rafa.rustcraft.item.custom;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.rafa.rustcraft.block.ModBlocks;

import java.util.Map;

public class HammerItem extends Item {

    private static final Map<Block, Block> HAMMER_CENTER_BLOCK =
            Map.of(
                    ModBlocks.WOODEN_BUILDING_BLOCK_CENTER, ModBlocks.STONE_BUILDING_BLOCK_CENTER,
                    ModBlocks.STONE_BUILDING_BLOCK_CENTER, Blocks.IRON_BLOCK,
                    Blocks.IRON_BLOCK, Blocks.NETHERITE_BLOCK
            );

    private static final Map<Block, Item> HAMMER_ITEM_MAP =
            Map.of(
                    ModBlocks.WOODEN_BUILDING_BLOCK_CENTER, Items.COBBLESTONE,
                    ModBlocks.STONE_BUILDING_BLOCK_CENTER, Items.IRON_INGOT,
                    Blocks.IRON_BLOCK, Items.NETHERITE_INGOT
            );

    private static final Map<Block, SoundEvent> HAMMER_SOUND_MAP =
            Map.of(
                    ModBlocks.WOODEN_BUILDING_BLOCK_CENTER, SoundEvents.ENTITY_IRON_GOLEM_REPAIR,
                    ModBlocks.STONE_BUILDING_BLOCK_CENTER, SoundEvents.BLOCK_SMITHING_TABLE_USE,
                    Blocks.IRON_BLOCK, SoundEvents.BLOCK_ANVIL_USE
            );

    public HammerItem(Settings settings) {
        super(settings);
    }

    private void changeBlock(World world, ItemUsageContext context, Block clickedBlock, ServerPlayerEntity player) {
        if (!world.isClient) {
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
                    if (count < 9) {
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
                        player.getInventory().removeStack(posAndAmount[k][0], 9);
                    for (int i = -1; i < 2; i++) {
                        for (int j = -1; j < 2; j++) {
                            if (i != 0 || j != 0)
                                world.setBlockState(context.getBlockPos().add(i, 0, j), HAMMER_CENTER_BLOCK.get(clickedBlock).getDefaultState());
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
                    for (int p = 0; p < count - 9; p++)
                        player.getInventory().insertStack(posAndAmount[k][0], HAMMER_ITEM_MAP.get(clickedBlock).getDefaultStack());

                }
            }
        }
    }

    private void removeFloor(World world, ItemUsageContext context, ServerPlayerEntity player) {
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                world.setBlockState(context.getBlockPos().add(i, 0, j), Blocks.AIR.getDefaultState());
            }
        }
        for (int n = 0; n < 9; n++)
            player.getInventory().offerOrDrop(Items.OAK_PLANKS.getDefaultStack());
    }

    private void removeWall(World world, ItemUsageContext context, ServerPlayerEntity player) {
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        Block clickedBlock = world.getBlockState(context.getBlockPos()).getBlock();
        if (!world.isClient) {
            ServerPlayerEntity player = ((ServerPlayerEntity) context.getPlayer());
            if (player != null) {
                if (player.isInSneakingPose() &&
                        clickedBlock.equals(ModBlocks.WOODEN_BUILDING_BLOCK_CENTER)) {
                    if (context.getSide().equals(Direction.DOWN) || context.getSide().equals(Direction.UP))
                        removeFloor(world, context, player);
                    else
                        removeWall(world, context, player);
                } else if (HAMMER_CENTER_BLOCK.containsKey(clickedBlock)) {
                    changeBlock(world, context, clickedBlock, player);
                } else {
                    player.playSoundToPlayer(SoundEvents.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, SoundCategory.BLOCKS, 1, 1);
                }
            }
        }
        return ActionResult.SUCCESS;
    }
}
