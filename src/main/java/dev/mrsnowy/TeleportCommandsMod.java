package dev.mrsnowy.teleport_commands;

import dev.mrsnowy.teleport_commands.commands.home;
import dev.mrsnowy.teleport_commands.commands.tpa;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

public class TeleportCommandsMod implements ModInitializer {
    @Override
    public void onInitialize() {
        TeleportCommands.MOD_LOADER = "Fabric";
        TeleportCommands.init();
        
        // 注册命令
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            home.register(dispatcher);
            tpa.register(dispatcher);
        });
        
        // 服务器生命周期事件
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            TeleportCommands.initializeServer(server.getSavePath(WorldSavePath.ROOT));
        });
        
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            TeleportCommands.shutdown();
        });
    }
}