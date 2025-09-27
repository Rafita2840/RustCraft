package net.rafa.rustcraft.screen;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.rafa.rustcraft.RustCraft;
import net.rafa.rustcraft.screen.custom.DefaultCrateScreenHandler;
import net.rafa.rustcraft.screen.custom.ResourceVendingMachineScreenHandler;

public class ModScreenHandlers {

    public static final ScreenHandlerType<DefaultCrateScreenHandler> CRATE_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, Identifier.of(RustCraft.MOD_ID, "crate_screen_handler"),
                    new ExtendedScreenHandlerType<>(DefaultCrateScreenHandler::new, BlockPos.PACKET_CODEC));

    public static final ScreenHandlerType<ResourceVendingMachineScreenHandler> RVM_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, Identifier.of(RustCraft.MOD_ID, "rvm_screen_handler"),
                    new ExtendedScreenHandlerType<>(ResourceVendingMachineScreenHandler::new, BlockPos.PACKET_CODEC));


    public static void registerScreenHandlers(){
        RustCraft.LOGGER.info("Registering Mod Screen Handlers for " + RustCraft.MOD_ID);
    }


}