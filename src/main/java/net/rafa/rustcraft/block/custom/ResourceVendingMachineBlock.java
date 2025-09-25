package net.rafa.rustcraft.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.village.Merchant;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.rafa.rustcraft.block.entity.custom.ResourceVendingMachineBlockEntity;
import net.rafa.rustcraft.merchant.BaseVendingMachineMerchant;
import net.rafa.rustcraft.merchant.TradeUtil;
import org.jetbrains.annotations.Nullable;

public class ResourceVendingMachineBlock extends BlockWithEntity implements BlockEntityProvider {

    public static final EnumProperty<DoubleBlockHalf> HALF = EnumProperty.of("half", DoubleBlockHalf.class);
    public static final MapCodec<ResourceVendingMachineBlock> CODEC = createCodec(ResourceVendingMachineBlock::new);
    public static final DirectionProperty FACING = Properties.FACING;

    public ResourceVendingMachineBlock(Settings settings) {
        super(settings);
        setDefaultState(this.getStateManager().getDefaultState().with(FACING, Direction.NORTH).with(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        DoubleBlockHalf half = state.get(HALF);
        Direction facing = state.get(FACING);
        if (half == DoubleBlockHalf.LOWER) {
            return switch (facing) {
                case NORTH -> VoxelShapes.cuboid(0, 0, 0.5f, 1f, 1.875f, 1f);
                case EAST -> VoxelShapes.cuboid(0, 0, 0, 0.5f, 1.875f, 1f);
                case WEST -> VoxelShapes.cuboid(0.5f, 0, 0, 1f, 1.875f, 1f);
                default -> VoxelShapes.cuboid(0, 0, 0, 1f, 1.875f, 0.5f);
            };
        }
        else {
            return switch (facing) {
                case NORTH -> VoxelShapes.cuboid(0, -1, 0.5f, 1f, 0.875f, 1f);
                case EAST -> VoxelShapes.cuboid(0, -1, 0, 0.5f, 0.875f, 1f);
                case WEST -> VoxelShapes.cuboid(0.5f, -1, 0, 1f, 0.875f, 1f);
                default -> VoxelShapes.cuboid(0, -1, 0, 1f, 0.875f, 0.5f);
            };
        }
    }

    @Override
    protected MapCodec<ResourceVendingMachineBlock> getCodec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction facing = ctx.getHorizontalPlayerFacing().getOpposite();
        BlockPos pos = ctx.getBlockPos();
        World world = ctx.getWorld();

        if (pos.getY() < world.getTopY() - 1 && world.getBlockState(pos.up()).canReplace(ctx)) {
            return this.getDefaultState()
                    .with(FACING, facing)
                    .with(HALF, DoubleBlockHalf.LOWER);
        }
        return null;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (!world.isClient)
            world.setBlockState(pos.up(), state.with(HALF, DoubleBlockHalf.UPPER), 3);
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        DoubleBlockHalf half = state.get(HALF);
        BlockPos otherPos = (half == DoubleBlockHalf.LOWER) ? pos.up() : pos.down();
        BlockState otherState = world.getBlockState(otherPos);
        if (otherState.isOf(this) && otherState.get(HALF) != half) {
            world.breakBlock(otherPos, false, player);
        }
        super.onBreak(world, pos, state, player);
        return otherState;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            BaseVendingMachineMerchant merchant = new BaseVendingMachineMerchant((ServerPlayerEntity) player, TradeUtil.getResourcesTradeOffers());
            merchant.sendOffers(player, Text.of("Resource Vending Machine"), merchant.getLevelProgress());
        }
        return ActionResult.SUCCESS;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ResourceVendingMachineBlockEntity(pos, state);
    }
}
