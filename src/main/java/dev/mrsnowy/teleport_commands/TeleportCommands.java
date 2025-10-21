package dev.mrsnowy.teleport_commands;

import dev.mrsnowy.teleport_commands.commands.home;
import dev.mrsnowy.teleport_commands.commands.tpa;
import dev.mrsnowy.teleport_commands.commands.warp;
import dev.mrsnowy.teleport_commands.storage.StorageManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class TeleportCommands implements ModInitializer {
    public static final String MOD_ID = "teleport_commands";
    public static final String MOD_NAME = "Teleport Commands";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    public static String MOD_LOADER;
    public static Path SAVE_DIR;

    @Override
    public void onInitialize() {
        MOD_LOADER = "Fabric";
        init();
        
        // 注册命令
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            home.register(dispatcher);
            tpa.register(dispatcher);
            warp.register(dispatcher); // 注册warp命令
        });
        
        // 服务器生命周期事件
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            initializeServer(server.getSavePath(WorldSavePath.ROOT));
        });
        
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            shutdown();
        });
    }

    public static void init() {
        LOGGER.info("TeleportCommands mod is loading...");
    }
    
    public static void initializeServer(Path worldPath) {
        SAVE_DIR = worldPath;
        try {
            StorageManager.StorageInit();
            LOGGER.info("Storage initialized successfully");
        } catch (Exception e) {
            LOGGER.error("Failed to initialize storage", e);
        }
    }
    
    public static void shutdown() {
        StorageManager.StorageCloseToSave();
    }
}