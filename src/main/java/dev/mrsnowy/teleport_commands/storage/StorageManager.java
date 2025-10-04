package dev.mrsnowy.teleport_commands.storage;

import com.google.gson.*;
import dev.mrsnowy.teleport_commands.TeleportCommands;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class StorageManager {
    public static Path STORAGE_FILE;
    private static JsonObject storageData;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void StorageInit() throws IOException {
        STORAGE_FILE = TeleportCommands.SAVE_DIR.resolve("teleport_commands.json");
        
        if (!Files.exists(STORAGE_FILE)) {
            // 创建默认结构
            JsonObject defaultData = new JsonObject();
            defaultData.add("Players", new JsonArray());
            
            String json = GSON.toJson(defaultData);
            Files.write(STORAGE_FILE, json.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            TeleportCommands.LOGGER.info("Created new storage file: {}", STORAGE_FILE);
        }
        
        loadStorage();
    }

    private static void loadStorage() throws IOException {
        try (FileReader reader = new FileReader(STORAGE_FILE.toFile())) {
            JsonElement element = JsonParser.parseReader(reader);
            if (element.isJsonObject()) {
                storageData = element.getAsJsonObject();
            } else {
                storageData = new JsonObject();
                storageData.add("Players", new JsonArray());
            }
        }
    }

    public static void StorageCloseToSave() {
        try {
            saveStorage();
            TeleportCommands.LOGGER.info("Storage saved successfully");
        } catch (IOException e) {
            TeleportCommands.LOGGER.error("Failed to save storage", e);
        }
    }

    private static void saveStorage() throws IOException {
        String json = GSON.toJson(storageData);
        Files.write(STORAGE_FILE, json.getBytes(), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    // 获取或创建玩家数据
    public static JsonObject getPlayerData(String uuid) {
        JsonArray players = storageData.getAsJsonArray("Players");
        
        for (JsonElement element : players) {
            if (element.isJsonObject()) {
                JsonObject player = element.getAsJsonObject();
                if (player.has("UUID") && player.get("UUID").getAsString().equals(uuid)) {
                    return player;
                }
            }
        }
        
        // 创建新玩家数据 - 简化版本，直接存储家园坐标
        JsonObject newPlayer = new JsonObject();
        newPlayer.addProperty("UUID", uuid);
        newPlayer.addProperty("home_x", 0);
        newPlayer.addProperty("home_y", 0);
        newPlayer.addProperty("home_z", 0);
        newPlayer.addProperty("home_world", "");
        newPlayer.addProperty("has_home", false);
        
        players.add(newPlayer);
        return newPlayer;
    }

    // 设置家园 - 直接覆盖，不需要名称参数
    public static void setPlayerHome(String uuid, int x, int y, int z, String world) {
        JsonObject playerData = getPlayerData(uuid);
        
        // 直接覆盖家园位置
        playerData.addProperty("home_x", x);
        playerData.addProperty("home_y", y);
        playerData.addProperty("home_z", z);
        playerData.addProperty("home_world", world);
        playerData.addProperty("has_home", true);
        
        try {
            saveStorage();
        } catch (IOException e) {
            TeleportCommands.LOGGER.error("Failed to save player home", e);
        }
    }

    // 获取家园位置
    public static JsonObject getPlayerHome(String uuid) {
        JsonObject playerData = getPlayerData(uuid);
        
        // 检查是否有家园
        if (!playerData.has("has_home") || !playerData.get("has_home").getAsBoolean()) {
            return null;
        }
        
        // 返回家园数据
        JsonObject home = new JsonObject();
        home.addProperty("x", playerData.get("home_x").getAsInt());
        home.addProperty("y", playerData.get("home_y").getAsInt());
        home.addProperty("z", playerData.get("home_z").getAsInt());
        home.addProperty("world", playerData.get("home_world").getAsString());
        
        return home;
    }
}