package net.rafa.rustcraft.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.rafa.rustcraft.block.entity.custom.VendingMachineBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * The Vending Machine block – lower half.
 *
 * Behaviour:
 *  • Placed like a door: occupies 2 blocks vertically (lower + upper).
 *  • Faces the direction the player is looking (N/S/E/W).
 *  • Right-click opens the trade GUI.
 *  • Breaking either half removes both halves.
 */
public class VendingMachineBlock extends BlockWithEntity {

    public static final MapCodec<VendingMachineBlock> CODEC = createCodec(VendingMachineBlock::new);

    /** Which horizontal direction the machine faces */
    public static final DirectionProperty FACING =
            HorizontalFacingBlock.FACING;

    /** Whether this is the lower or upper half */
    public static final EnumProperty<DoubleBlockHalf> HALF =
            Properties.DOUBLE_BLOCK_HALF;

    public VendingMachineBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    // ── Block state ────────────────────────────────────────────────

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF);
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos pos = ctx.getBlockPos();
        World world = ctx.getWorld();
        // Need the block above to be empty
        if (pos.getY() < world.getTopY() - 1
                && world.getBlockState(pos.up()).canReplace(ctx)) {
            return getDefaultState()
                    .with(FACING, ctx.getHorizontalPlayerFacing().getOpposite())
                    .with(HALF, DoubleBlockHalf.LOWER);
        }
        return null; // Can't place
    }

    // ── Placement: put the upper half automatically ─────────────────

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state,
                         @Nullable LivingEntity placer, ItemStack itemStack) {
        if (!world.isClient) {
            BlockPos upperPos = pos.up();
            world.setBlockState(upperPos,
                    state.with(HALF, DoubleBlockHalf.UPPER),
                    Block.NOTIFY_ALL);
            world.updateNeighbors(pos, Blocks.AIR);
            state.updateNeighbors(world, pos, Block.NOTIFY_ALL);
        }
    }

    // ── Breaking: remove both halves ───────────────────────────────

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient) {
            DoubleBlockHalf half = state.get(HALF);
            BlockPos otherPos = half == DoubleBlockHalf.LOWER ? pos.up() : pos.down();
            BlockState otherState = world.getBlockState(otherPos);

            if (otherState.getBlock() == this
                    && otherState.get(HALF) != half) {
                world.setBlockState(otherPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL | Block.SKIP_DROPS);
            }
        }
        return super.onBreak(world, pos, state, player);
    }

    // ── Interaction: open GUI only from the lower half ─────────────

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos,
                                 PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;

        // Always open the GUI from the lower half's block entity
        BlockPos lowerPos = state.get(HALF) == DoubleBlockHalf.LOWER ? pos : pos.down();
        NamedScreenHandlerFactory factory = world.getBlockState(lowerPos)
                .createScreenHandlerFactory(world, lowerPos);

        if (factory != null) {
            player.openHandledScreen(factory);
        }
        return ActionResult.CONSUME;
    }

    // ── Rotation / mirror ──────────────────────────────────────────

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    // ── Block entity ───────────────────────────────────────────────

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        // Only the lower half has a block entity
        if (state.get(HALF) == DoubleBlockHalf.LOWER) {
            return new VendingMachineBlockEntity(pos, state);
        }
        return null;
    }

    @Nullable
    @Override
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        return be instanceof NamedScreenHandlerFactory factory ? factory : null;
    }
}