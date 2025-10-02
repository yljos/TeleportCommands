package dev.mrsnowy.teleport_commands;

import dev.mrsnowy.teleport_commands.storage.StorageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class TeleportCommands {
    public static final String MOD_ID = "teleport_commands";
    public static final String MOD_NAME = "Teleport Commands";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    public static String MOD_LOADER;
    public static Path SAVE_DIR;

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