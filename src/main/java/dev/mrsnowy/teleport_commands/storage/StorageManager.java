package dev.mrsnowy.teleport_commands.storage;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StorageManager {
    private static final Gson GSON = new Gson();
    private static Path dataDir;

    public static void StorageInit() {
        // 初始化数据目录
        dataDir = Paths.get("teleport_commands_data");
        try {
            Files.createDirectories(dataDir);
        } catch (IOException e) {
            System.err.println("Failed to create data directory: " + e.getMessage());
        }
    }

    public static void StorageCloseToSave() {
        // 保存数据逻辑（如果需要）
    }

    public static void setPlayerHome(String playerUUID, int x, int y, int z, String world) {
        JsonObject homeData = new JsonObject();
        homeData.addProperty("x", x);
        homeData.addProperty("y", y);
        homeData.addProperty("z", z);
        homeData.addProperty("world", world);

        Path homeFile = dataDir.resolve(playerUUID + "_home.json");
        try (FileWriter writer = new FileWriter(homeFile.toFile())) {
            GSON.toJson(homeData, writer);
        } catch (IOException e) {
            System.err.println("Failed to save home data: " + e.getMessage());
        }
    }

    public static JsonObject getPlayerHome(String playerUUID) {
        Path homeFile = dataDir.resolve(playerUUID + "_home.json");
        if (!Files.exists(homeFile)) {
            return null;
        }

        try (FileReader reader = new FileReader(homeFile.toFile())) {
            return GSON.fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            System.err.println("Failed to load home data: " + e.getMessage());
            return null;
        }
    }
}