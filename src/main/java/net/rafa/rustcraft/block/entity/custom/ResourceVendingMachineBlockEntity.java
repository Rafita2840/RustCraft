package net.rafa.rustcraft.block.entity.custom;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.rafa.rustcraft.block.entity.ModBlockEntities;
import net.rafa.rustcraft.merchant.BaseVendingMachineMerchant;
import net.rafa.rustcraft.merchant.TradeUtil;
import net.rafa.rustcraft.screen.custom.ResourceVendingMachineScreenHandler;
import org.jetbrains.annotations.Nullable;

public class ResourceVendingMachineBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory<BlockPos> {

    public ResourceVendingMachineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RVM_BE, pos, state);
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayerEntity serverPlayerEntity) {
        return this.pos;
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Resource Vending Machine");
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new ResourceVendingMachineScreenHandler(syncId, playerInventory, pos);
    }

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }

}
