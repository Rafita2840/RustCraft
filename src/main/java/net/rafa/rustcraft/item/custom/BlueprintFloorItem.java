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
import net.minecraft.world.World;
import net.rafa.rustcraft.block.ModBlocks;
import net.rafa.rustcraft.item.ModItems;

public class BlueprintFloorItem extends Item {

    private static final Item RESOURCE_NEEDED = ModItems.WOOD;


    public BlueprintFloorItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient){
            ServerPlayerEntity player = ((ServerPlayerEntity) user);
            if (player.isSneaking()){
                int pos = player.getInventory().getSlotWithStack(player.getStackInHand(hand));
                player.getInventory().removeStack(pos);
                player.getInventory().insertStack(pos, ModItems.BLUEPRINT_WALL.getDefaultStack());
            }
        }
        return super.use(world, user, hand);
    }


    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        Block clickedBlock = world.getBlockState(context.getBlockPos()).getBlock();
        Block[] surroundings = new Block[8];
        Block[] upSurroundings = new Block[9];
        if (!world.isClient) {
            ServerPlayerEntity player = ((ServerPlayerEntity) context.getPlayer());
            if (player != null) {
                if (clickedBlock.equals(Blocks.SANDSTONE)) {
                    surroundings[0] = world.getBlockState(context.getBlockPos().add(1, 0, 1)).getBlock();
                    surroundings[1] = world.getBlockState(context.getBlockPos().add(-1, 0, -1)).getBlock();
                    surroundings[2] = world.getBlockState(context.getBlockPos().add(1, 0, -1)).getBlock();
                    surroundings[3] = world.getBlockState(context.getBlockPos().add(-1, 0, 1)).getBlock();
                    surroundings[4] = world.getBlockState(context.getBlockPos().add(0, 0, 1)).getBlock();
                    surroundings[5] = world.getBlockState(context.getBlockPos().add(0, 0, -1)).getBlock();
                    surroundings[6] = world.getBlockState(context.getBlockPos().add(-1, 0, 0)).getBlock();
                    surroundings[7] = world.getBlockState(context.getBlockPos().add(1, 0, 0)).getBlock();
                    upSurroundings[0] = world.getBlockState(context.getBlockPos().add(1, 1, 1)).getBlock();
                    upSurroundings[1] = world.getBlockState(context.getBlockPos().add(-1, 1, -1)).getBlock();
                    upSurroundings[2] = world.getBlockState(context.getBlockPos().add(1, 1, -1)).getBlock();
                    upSurroundings[3] = world.getBlockState(context.getBlockPos().add(-1, 1, 1)).getBlock();
                    upSurroundings[4] = world.getBlockState(context.getBlockPos().add(0, 1, 1)).getBlock();
                    upSurroundings[5] = world.getBlockState(context.getBlockPos().add(0, 1, -1)).getBlock();
                    upSurroundings[6] = world.getBlockState(context.getBlockPos().add(-1, 1, 0)).getBlock();
                    upSurroundings[7] = world.getBlockState(context.getBlockPos().add(1, 1, 0)).getBlock();
                    upSurroundings[8] = world.getBlockState(context.getBlockPos().add(0, 1, 0)).getBlock();
                    boolean same = false, otherSame = false;
                    int counter = 0;
                    int i = 0;
                    while (!same) {
                        if (surroundings[i].equals(clickedBlock))
                            counter++;
                        i++;
                        if (counter == 8)
                            same = true;
                        if (i == 8)
                            break;
                    }
                    i = 0;
                    counter = 0;
                    while (!otherSame) {
                        if (upSurroundings[i].equals(Blocks.AIR))
                            counter++;
                        i++;
                        if (counter == 9)
                            otherSame = true;
                        if (i == 9)
                            break;
                    }
                    if (same && otherSame) {
                        placeFloor(world, context);
                    } else {
                        player.playSoundToPlayer(SoundEvents.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, SoundCategory.BLOCKS, 5, 1);
                    }
                } else {
                    player.playSoundToPlayer(SoundEvents.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, SoundCategory.BLOCKS, 5, 1);
                    player.sendMessage(Text.translatable("text.rustcraft.cant_use"), true);
                }
            }
        }
        return ActionResult.SUCCESS;
    }

    private void placeFloor(World world, ItemUsageContext context) {
        if (!world.isClient) {
            ServerPlayerEntity player = ((ServerPlayerEntity) context.getPlayer());
            if (player != null) {
                int[][] posAndAmount = new int[9][2];
                int k = 0;
                int count = 0;
                boolean success = false;
                while (!success && k < 9) {
                    posAndAmount[k][0] = player.getInventory().getSlotWithStack(RESOURCE_NEEDED.getDefaultStack());
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
                                world.setBlockState(context.getBlockPos().add(i, 1, j), ModBlocks.WOODEN_BUILDING_BLOCK.getDefaultState());
                        }
                    }
                    world.setBlockState(context.getBlockPos().add(0, 1, 0), ModBlocks.WOODEN_BUILDING_BLOCK_CENTER.getDefaultState());
                    player.playSoundToPlayer(SoundEvents.ENTITY_VILLAGER_WORK_FLETCHER, SoundCategory.BLOCKS, 20, 1);
                } else  {
                    player.playSoundToPlayer(SoundEvents.UI_STONECUTTER_TAKE_RESULT, SoundCategory.BLOCKS, 20, 1);
                }
                if (k > 0 && !success){
                    for (int n = 0; n < k; n++){
                        for (int l = 0; l < posAndAmount[n][1]; l++)
                            player.getInventory().insertStack(posAndAmount[n][0], RESOURCE_NEEDED.getDefaultStack());
                    }
                }
                if (k > 0 && success){
                    for (int p = 0; p < count - 9; p++)
                        player.getInventory().insertStack(posAndAmount[k][0], RESOURCE_NEEDED.getDefaultStack());

                }
            }
        }
    }
}
