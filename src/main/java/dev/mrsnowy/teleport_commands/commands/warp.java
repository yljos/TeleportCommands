package dev.mrsnowy.teleport_commands.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.mrsnowy.teleport_commands.TeleportCommands;
import dev.mrsnowy.teleport_commands.storage.StorageManager;
import dev.mrsnowy.teleport_commands.suggestions.WarpSuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static dev.mrsnowy.teleport_commands.storage.StorageManager.*;
import static dev.mrsnowy.teleport_commands.utils.tools.Teleporter;
import static dev.mrsnowy.teleport_commands.utils.tools.getTranslatedText;
import static net.minecraft.commands.Commands.argument;

public class warp {

    public static void register(Commands commandManager) {

        commandManager.getDispatcher().register(Commands.literal("setwarp")
                .then(argument("name", StringArgumentType.string())
                        .executes(context -> {
                            final String name = StringArgumentType.getString(context, "name");
                            final ServerPlayer player = context.getSource().getPlayerOrException();

                            try {
                                SetWarp(player, name);
                            } catch (Exception e) {
                                TeleportCommands.LOGGER.error("Error while setting the warp!", e);
                                player.displayClientMessage(getTranslatedText("commands.teleport_commands.warp.setError", player).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
                                return 1;
                            }
                            return 0;
                        })));

        commandManager.getDispatcher().register(Commands.literal("warp")
                .then(argument("name", StringArgumentType.string())
                        .suggests(new WarpSuggestionProvider())
                        .executes(context -> {
                            final String name = StringArgumentType.getString(context, "name");
                            final ServerPlayer player = context.getSource().getPlayerOrException();

                            try {
                                GoToWarp(player, name);
                            } catch (Exception e) {
                                TeleportCommands.LOGGER.error("Error while going to the warp!", e);
                                player.displayClientMessage(getTranslatedText("commands.teleport_commands.warp.goError", player).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
                                return 1;
                            }
                            return 0;
                        })));

        commandManager.getDispatcher().register(Commands.literal("delwarp")
                .then(argument("name", StringArgumentType.string())
                        .suggests(new WarpSuggestionProvider())
                        .executes(context -> {
                            final String name = StringArgumentType.getString(context, "name");
                            final ServerPlayer player = context.getSource().getPlayerOrException();

                            try {
                                DeleteWarp(player, name);
                            } catch (Exception e) {
                                TeleportCommands.LOGGER.error("Error while deleting the warp!", e);
                                player.displayClientMessage(getTranslatedText("commands.teleport_commands.warp.deleteError", player).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
                                return 1;
                            }
                            return 0;
                        })));

        commandManager.getDispatcher().register(Commands.literal("warps")
                .executes(context -> {
                    final ServerPlayer player = context.getSource().getPlayerOrException();

                    try {
                        PrintWarps(player);
                    } catch (Exception e) {
                        TeleportCommands.LOGGER.error("Error while printing warps!", e);
                        player.displayClientMessage(getTranslatedText("commands.teleport_commands.warps.error", player).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
                        return 1;
                    }
                    return 0;
                }));
    }


    private static void SetWarp(ServerPlayer player, String warpName) throws Exception {
        final String finalWarpName = warpName.toLowerCase();
        
        StorageManager.PlayerStorageClass storages = GetPlayerStorage(player.getStringUUID());
        StorageManager.StorageClass storage = storages.storage;
        
        // 添加Warps列表到存储中（如果不存在）
        if (storage.Warps == null) {
            storage.Warps = new ArrayList<>();
        }
        
        BlockPos blockPos = new BlockPos(player.getBlockX(), player.getBlockY(), player.getBlockZ());
        String worldString = player.level().dimension().location().toString();
        
        // 检查是否已存在同名warp
        boolean warpExists = storage.Warps.stream()
                .anyMatch(warp -> Objects.equals(warp.name, finalWarpName));
        
        if (warpExists) {
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.warp.exists", player).withStyle(ChatFormatting.RED), true);
        } else {
            // 创建新的warp
            StorageManager.StorageClass.Warp newWarp = new StorageManager.StorageClass.Warp();
            newWarp.name = finalWarpName;
            newWarp.x = blockPos.getX();
            newWarp.y = blockPos.getY();
            newWarp.z = blockPos.getZ();
            newWarp.world = worldString;
            
            storage.Warps.add(newWarp);
            
            StorageSaver(storage);
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.warp.set", player), true);
        }
    }

    private static void GoToWarp(ServerPlayer player, String warpName) throws Exception {
        final String finalWarpName = warpName.toLowerCase();
        
        StorageManager.PlayerStorageClass storages = GetPlayerStorage(player.getStringUUID());
        StorageManager.StorageClass storage = storages.storage;
        
        if (storage.Warps == null) {
            storage.Warps = new ArrayList<>();
        }
        
        // 查找warp
        Optional<StorageManager.StorageClass.Warp> optionalWarp = storage.Warps.stream()
                .filter(warp -> Objects.equals(warp.name, finalWarpName))
                .findFirst();
        
        if (optionalWarp.isEmpty()) {
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.warp.notFound", player).withStyle(ChatFormatting.RED), true);
            return;
        }
        
        StorageManager.StorageClass.Warp warp = optionalWarp.get();
        
        // 查找世界
        ServerLevel warpWorld = null;
        for (ServerLevel currentWorld : Objects.requireNonNull(player.getServer()).getAllLevels()) {
            if (Objects.equals(currentWorld.dimension().location().toString(), warp.world)) {
                warpWorld = currentWorld;
                break;
            }
        }
        
        if (warpWorld == null) {
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.common.error", player).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
            return;
        }
        
        BlockPos warpPos = new BlockPos(warp.x, warp.y, warp.z);
        
        // 检查是否已在该位置
        if (player.blockPosition().equals(warpPos) && player.level() == warpWorld) {
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.home.goSame", player).withStyle(ChatFormatting.AQUA), true);
        } else {
            Vec3 teleportPos = new Vec3(warp.x + 0.5, warp.y, warp.z + 0.5);
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.common.teleport", player), true);
            Teleporter(player, warpWorld, teleportPos);
        }
    }

    private static void DeleteWarp(ServerPlayer player, String warpName) throws Exception {
        final String finalWarpName = warpName.toLowerCase();
        
        StorageManager.PlayerStorageClass storages = GetPlayerStorage(player.getStringUUID());
        StorageManager.StorageClass storage = storages.storage;
        
        if (storage.Warps == null) {
            storage.Warps = new ArrayList<>();
        }
        
        // 查找并删除warp
        boolean removed = storage.Warps.removeIf(warp -> Objects.equals(warp.name, finalWarpName));
        
        if (removed) {
            StorageSaver(storage);
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.warp.delete", player), true);
        } else {
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.warp.notFound", player).withStyle(ChatFormatting.RED), true);
        }
    }

    private static void PrintWarps(ServerPlayer player) throws Exception {
        StorageManager.PlayerStorageClass storages = GetPlayerStorage(player.getStringUUID());
        StorageManager.StorageClass storage = storages.storage;
        
        if (storage.Warps == null || storage.Warps.isEmpty()) {
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.warp.homeless", player).withStyle(ChatFormatting.AQUA), true);
            return;
        }
        
        player.displayClientMessage(getTranslatedText("commands.teleport_commands.warps.warps", player).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD), false);
        
        for (StorageManager.StorageClass.Warp currentWarp : storage.Warps) {
            String name = String.format("  - %s", currentWarp.name);
            String coords = String.format("[X%d Y%d Z%d]", currentWarp.x, currentWarp.y, currentWarp.z);
            String dimension = String.format(" [%s]", currentWarp.world);
            
            player.displayClientMessage(Component.literal(name).withStyle(ChatFormatting.AQUA), false);
            
            player.displayClientMessage(Component.literal("     | ").withStyle(ChatFormatting.AQUA)
                    .append(Component.literal(coords).withStyle(ChatFormatting.LIGHT_PURPLE)
                            .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, 
                                    String.format("X%d Y%d Z%d", currentWarp.x, currentWarp.y, currentWarp.z)))))
                    .append(Component.literal(dimension).withStyle(ChatFormatting.DARK_PURPLE)
                            .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, currentWarp.world)))), false);
            
            player.displayClientMessage(Component.literal("     | ").withStyle(ChatFormatting.AQUA)
                    .append(getTranslatedText("commands.teleport_commands.homes.tp", player).withStyle(ChatFormatting.GREEN)
                            .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/warp %s", currentWarp.name)))))
                    .append(" ")
                    .append(getTranslatedText("commands.teleport_commands.homes.delete", player).withStyle(ChatFormatting.RED)
                            .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("/delwarp %s", currentWarp.name)))))
                    .append("\n"), false);
        }
    }
}
