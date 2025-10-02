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
        
        // 创建新玩家数据
        JsonObject newPlayer = new JsonObject();
        newPlayer.addProperty("UUID", uuid);
        newPlayer.addProperty("DefaultHome", "");
        newPlayer.add("Homes", new JsonArray());
        
        players.add(newPlayer);
        return newPlayer;
    }

    public static void setPlayerHome(String uuid, String homeName, int x, int y, int z, String world) {
        JsonObject playerData = getPlayerData(uuid);
        JsonArray homes = playerData.getAsJsonArray("Homes");
        
        // 检查是否已存在同名家园
        JsonObject existingHome = null;
        for (JsonElement element : homes) {
            if (element.isJsonObject()) {
                JsonObject home = element.getAsJsonObject();
                if (home.has("name") && home.get("name").getAsString().equals(homeName)) {
                    existingHome = home;
                    break;
                }
            }
        }
        
        if (existingHome != null) {
            // 更新现有家园
            existingHome.addProperty("x", x);
            existingHome.addProperty("y", y);
            existingHome.addProperty("z", z);
            existingHome.addProperty("world", world);
        } else {
            // 创建新家园
            JsonObject newHome = new JsonObject();
            newHome.addProperty("name", homeName);
            newHome.addProperty("x", x);
            newHome.addProperty("y", y);
            newHome.addProperty("z", z);
            newHome.addProperty("world", world);
            homes.add(newHome);
        }
        
        playerData.addProperty("DefaultHome", homeName);
        
        try {
            saveStorage();
        } catch (IOException e) {
            TeleportCommands.LOGGER.error("Failed to save player home", e);
        }
    }

    public static JsonObject getPlayerHome(String uuid) {
        JsonObject playerData = getPlayerData(uuid);
        String defaultHome = playerData.has("DefaultHome") ? playerData.get("DefaultHome").getAsString() : "";
        JsonArray homes = playerData.getAsJsonArray("Homes");
        
        if (homes.size() == 0) {
            return null;
        }
        
        // 如果有默认家园，返回默认家园
        if (!defaultHome.isEmpty()) {
            for (JsonElement element : homes) {
                if (element.isJsonObject()) {
                    JsonObject home = element.getAsJsonObject();
                    if (home.has("name") && home.get("name").getAsString().equals(defaultHome)) {
                        return home;
                    }
                }
            }
        }
        
        // 否则返回第一个家园
        return homes.get(0).getAsJsonObject();
    }
}