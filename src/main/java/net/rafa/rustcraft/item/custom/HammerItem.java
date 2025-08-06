package net.rafa.rustcraft.item.custom;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;

import java.util.Map;

public class HammerItem extends Item {

    private static final Map<Block, Block> HAMMER_MAP =
            Map.of(
                    Blocks.GRASS_BLOCK, Blocks.OAK_PLANKS,
                    Blocks.OAK_PLANKS, Blocks.COBBLESTONE,
                    Blocks.COBBLESTONE, Blocks.IRON_BLOCK,
                    Blocks.IRON_BLOCK, Blocks.NETHERITE_BLOCK
            );

    private static final Map<Block, Item> HAMMER_ITEM_MAP =
            Map.of(
                    Blocks.GRASS_BLOCK, Items.OAK_PLANKS,
                    Blocks.OAK_PLANKS, Items.COBBLESTONE,
                    Blocks.COBBLESTONE, Items.IRON_INGOT,
                    Blocks.IRON_BLOCK, Items.NETHERITE_INGOT
            );

    public HammerItem(Settings settings) {
        super(settings);
    }

    private void changeBlock(World world, ItemUsageContext context, Block clickedBlock, int offset) {
        if (!world.isClient) {
            ServerPlayerEntity player = ((ServerPlayerEntity) context.getPlayer());
            if (player != null) {
                int count = 0;
                int counter = 0;
                while (counter < 9) {
                    if (player.getOffHandStack().isOf(HAMMER_ITEM_MAP.get(clickedBlock))) {
                        count++;
                        player.getOffHandStack().decrement(1);
                    }
                    counter++;
                }
                if (count == 9) {
                    for (int i = -1; i < 2; i++) {
                        for (int j = -1; j < 2; j++) {
                            world.setBlockState(context.getBlockPos().add(i, offset, j), HAMMER_MAP.get(clickedBlock).getDefaultState());
                        }
                    }
                    player.playSoundToPlayer(SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 50, 1);
                }
                else {
                    for (int k = 0; k < count; k++)
                        player.getInventory().insertStack(HAMMER_ITEM_MAP.get(clickedBlock).asItem().getDefaultStack());
                    player.playSoundToPlayer(SoundEvents.UI_STONECUTTER_TAKE_RESULT, SoundCategory.BLOCKS, 50, 1);
                }
            }
        }
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        Block clickedBlock = world.getBlockState(context.getBlockPos()).getBlock();
        Block[] corners = new Block[4];
        if (HAMMER_MAP.containsKey(clickedBlock)) {
            corners[0] = world.getBlockState(context.getBlockPos().add(1, 0, 1)).getBlock();
            corners[1] = world.getBlockState(context.getBlockPos().add(-1, 0, -1)).getBlock();
            corners[2] = world.getBlockState(context.getBlockPos().add(1, 0, -1)).getBlock();
            corners[3] = world.getBlockState(context.getBlockPos().add(-1, 0, 1)).getBlock();
            boolean same = false;
            int counter = 0;
            int i = 0;
            while (!same) {
                if (corners[i].equals(clickedBlock))
                    counter++;
                i++;
                if (counter == 4)
                    same = true;
                if (i == 4)
                    break;
            }
            if (same) {
                if (clickedBlock.equals(Blocks.GRASS_BLOCK))
                    changeBlock(world, context, clickedBlock, 1);
                else
                    changeBlock(world, context, clickedBlock, 0);
            } else {
                if (!world.isClient) {
                    ServerPlayerEntity player = ((ServerPlayerEntity) context.getPlayer());
                    if (player != null)
                        player.playSoundToPlayer(SoundEvents.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, SoundCategory.BLOCKS, 50, 1);
                }
            }
        }
        else {
            if (!world.isClient) {
                ServerPlayerEntity player = ((ServerPlayerEntity) context.getPlayer());
                if (player != null)
                    player.playSoundToPlayer(SoundEvents.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, SoundCategory.BLOCKS, 50, 1);
            }
        }
        return ActionResult.SUCCESS;
    }
}
