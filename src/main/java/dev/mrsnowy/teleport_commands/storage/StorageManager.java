package dev.mrsnowy.teleport_commands.storage;

import com.google.gson.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class StorageManager {
    // 使用相对目录 teleport_commands_data（运行目录下的根目录）
    private static final Path DATA_DIR = Paths.get("teleport_commands_data").toAbsolutePath().normalize();

    // 存储文件放在 DATA_DIR/teleport_commands.json
    public static Path STORAGE_FILE;
    private static JsonObject storageData;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // 无参初始化：始终使用运行目录下的 DATA_DIR
    public static void StorageInit() throws IOException {
        // 确保目录存在
        if (!Files.exists(DATA_DIR)) {
            Files.createDirectories(DATA_DIR);
        }

        STORAGE_FILE = DATA_DIR.resolve("teleport_commands.json");

        if (!Files.exists(STORAGE_FILE)) {
            // 创建默认结构，包含 Players 和 Warps
            JsonObject defaultData = new JsonObject();
            defaultData.add("Players", new JsonArray());
            defaultData.add("Warps", new JsonObject());

            String json = GSON.toJson(defaultData);
            Files.write(STORAGE_FILE, json.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        }

        loadStorage();

        // 确保 Warps 对象存在（兼容旧文件）
        if (!storageData.has("Warps") || !storageData.get("Warps").isJsonObject()) {
            storageData.add("Warps", new JsonObject());
            saveStorage();
        }
    }

    private static void loadStorage() throws IOException {
        try (FileReader reader = new FileReader(STORAGE_FILE.toFile())) {
            JsonElement element = JsonParser.parseReader(reader);
            if (element != null && element.isJsonObject()) {
                storageData = element.getAsJsonObject();
            } else {
                storageData = new JsonObject();
                storageData.add("Players", new JsonArray());
                storageData.add("Warps", new JsonObject());
            }
        }
    }

    public static void StorageCloseToSave() {
        try {
            saveStorage();
        } catch (IOException e) {
            // 可在此记录错误
        }
    }

    private static void saveStorage() throws IOException {
        // 确保目录存在（防止意外删除）
        if (!Files.exists(DATA_DIR)) {
            Files.createDirectories(DATA_DIR);
        }

        String json = GSON.toJson(storageData);
        Files.write(STORAGE_FILE, json.getBytes(), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    // ----------------- 玩家 home 相关（保持原有逻辑） -----------------
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

    public static void setPlayerHome(String uuid, int x, int y, int z, String world) {
        JsonObject playerData = getPlayerData(uuid);

        playerData.addProperty("home_x", x);
        playerData.addProperty("home_y", y);
        playerData.addProperty("home_z", z);
        playerData.addProperty("home_world", world);
        playerData.addProperty("has_home", true);

        try {
            saveStorage();
        } catch (IOException e) {
        }
    }

    public static JsonObject getPlayerHome(String uuid) {
        JsonObject playerData = getPlayerData(uuid);

        if (!playerData.has("has_home") || !playerData.get("has_home").getAsBoolean()) {
            return null;
        }

        JsonObject home = new JsonObject();
        home.addProperty("x", playerData.get("home_x").getAsInt());
        home.addProperty("y", playerData.get("home_y").getAsInt());
        home.addProperty("z", playerData.get("home_z").getAsInt());
        home.addProperty("world", playerData.get("home_world").getAsString());

        return home;
    }

    // ----------------- Warp 相关 API -----------------

    // 设置/覆盖 warp
    public static void setWarp(String name, int x, int y, int z, String world) {
        JsonObject warps = storageData.getAsJsonObject("Warps");
        if (warps == null) {
            warps = new JsonObject();
            storageData.add("Warps", warps);
        }

        JsonObject warp = new JsonObject();
        warp.addProperty("x", x);
        warp.addProperty("y", y);
        warp.addProperty("z", z);
        warp.addProperty("world", world);

        warps.add(name, warp);

        try {
            saveStorage();
        } catch (IOException e) {
            // 忽略或记录
        }
    }

    // 获取 warp（null 表示不存在）
    public static JsonObject getWarp(String name) {
        JsonObject warps = storageData.getAsJsonObject("Warps");
        if (warps == null) return null;
        JsonElement el = warps.get(name);
        if (el == null || !el.isJsonObject()) return null;
        return el.getAsJsonObject();
    }

    // 删除 warp，返回是否成功删除（存在则删除并返回 true）
    public static boolean delWarp(String name) {
        JsonObject warps = storageData.getAsJsonObject("Warps");
        if (warps == null) return false;
        if (!warps.has(name)) return false;
        warps.remove(name);
        try {
            saveStorage();
        } catch (IOException e) {
            // 忽略或记录
        }
        return true;
    }

    // 列出所有 warp 名称
    public static Set<String> listWarpNames() {
        Set<String> names = new HashSet<>();
        JsonObject warps = storageData.getAsJsonObject("Warps");
        if (warps == null) return names;
        for (String key : warps.keySet()) {
            names.add(key);
        }
        return names;
    }

    // 供外部访问 dataDir（例如 warp suggestion provider）
    public static Path getDataDir() {
        return DATA_DIR;
    }
}