package dev.mrsnowy.teleport_commands;

import dev.mrsnowy.teleport_commands.commands.home;
import dev.mrsnowy.teleport_commands.commands.tpa;
import dev.mrsnowy.teleport_commands.commands.warp;
import dev.mrsnowy.teleport_commands.storage.StorageManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeleportCommands implements ModInitializer {
    public static final String MOD_ID = "teleport_commands";
    public static final String MOD_NAME = "Teleport Commands";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    @Override
    public void onInitialize() {
        LOGGER.info("TeleportCommands (Fabric) initializing...");

        // 注册命令
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            try {
                home.register(dispatcher);
            } catch (Exception e) {
                LOGGER.error("Failed to register home command", e);
            }
            try {
                tpa.register(dispatcher);
            } catch (Exception e) {
                LOGGER.error("Failed to register tpa command", e);
            }
            try {
                warp.register(dispatcher);
            } catch (Exception e) {
                LOGGER.error("Failed to register warp commands", e);
            }
        });

        // 服务器启动时初始化存储：使用运行目录下的 teleport_commands_data
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            try {
                StorageManager.StorageInit();
                LOGGER.info("Storage initialized successfully at " + StorageManager.getDataDir());
            } catch (Exception e) {
                LOGGER.error("Failed to initialize storage", e);
            }
        });

        // 服务器停止时保存存储
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            try {
                StorageManager.StorageCloseToSave();
            } catch (Exception e) {
                LOGGER.error("Error during shutdown", e);
            }
        });
    }
}