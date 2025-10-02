package dev.mrsnowy.teleport_commands.commands;

import dev.mrsnowy.teleport_commands.TeleportCommands;
import dev.mrsnowy.teleport_commands.storage.StorageManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

import static dev.mrsnowy.teleport_commands.utils.tools.getTranslatedText;
import static dev.mrsnowy.teleport_commands.storage.StorageManager.GetPlayerStorage;
import static dev.mrsnowy.teleport_commands.storage.StorageManager.StorageSaver;
import static dev.mrsnowy.teleport_commands.utils.tools.Teleporter;

public class home {
    public static void register(Commands commandManager) {

        // 设置家园位置
        commandManager.getDispatcher().register(Commands.literal("sethome")
                .executes(context -> {
                    final ServerPlayer player = context.getSource().getPlayerOrException();

                    try {
                        SetHome(player);
                    } catch (Exception e) {
                        TeleportCommands.LOGGER.error(String.valueOf(e));
                        player.displayClientMessage(
                            getTranslatedText("commands.teleport_commands.home.setError", player)
                                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD), 
                            true);
                        return 1;
                    }
                    return 0;
                }));

        // 传送到家园
        commandManager.getDispatcher().register(Commands.literal("home")
                .executes(context -> {
                    final ServerPlayer player = context.getSource().getPlayerOrException();

                    try {
                        GoHome(player);
                    } catch (Exception e) {
                        TeleportCommands.LOGGER.error(String.valueOf(e));
                        player.displayClientMessage(
                            getTranslatedText("commands.teleport_commands.home.goError", player)
                                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD), 
                            true);
                        return 1;
                    }
                    return 0;
                }));

        // 删除家园
        commandManager.getDispatcher().register(Commands.literal("delhome")
                .executes(context -> {
                    final ServerPlayer player = context.getSource().getPlayerOrException();

                    try {
                        DeleteHome(player);
                    } catch (Exception e) {
                        TeleportCommands.LOGGER.error(String.valueOf(e));
                        player.displayClientMessage(
                            getTranslatedText("commands.teleport_commands.home.deleteError", player)
                                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD), 
                            true);
                        return 1;
                    }
                    return 0;
                }));
    }

    private static void SetHome(ServerPlayer player) throws Exception {
        BlockPos blockPos = new BlockPos(player.getBlockX(), player.getBlockY(), player.getBlockZ());
        ServerLevel world = player.serverLevel();

        StorageManager.PlayerStorageClass storages = GetPlayerStorage(player.getStringUUID());
        StorageManager.StorageClass storage = storages.storage;
        StorageManager.StorageClass.Player playerStorage = storages.playerStorage;

        // 清空现有的家园列表，只保留一个
        playerStorage.Homes.clear();

        // 创建新的家园位置
        StorageManager.StorageClass.Player.Home homeLocation = new StorageManager.StorageClass.Player.Home();
        homeLocation.name = "home";
        homeLocation.x = blockPos.getX();
        homeLocation.y = blockPos.getY();
        homeLocation.z = blockPos.getZ();
        homeLocation.world = world.dimension().location().toString();

        playerStorage.Homes.add(homeLocation);
        playerStorage.DefaultHome = "home";

        StorageSaver(storage);
        player.displayClientMessage(
            Component.literal("家园位置已设置: " + blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ())
                .withStyle(ChatFormatting.GREEN), 
            true);
    }

    private static void GoHome(ServerPlayer player) throws Exception {
        StorageManager.StorageClass.Player playerStorage = GetPlayerStorage(player.getStringUUID()).playerStorage;

        if (playerStorage.Homes.isEmpty()) {
            player.displayClientMessage(
                Component.literal("你还没有设置家园位置，使用 /sethome 来设置")
                    .withStyle(ChatFormatting.AQUA), 
                true);
            return;
        }

        StorageManager.StorageClass.Player.Home home = playerStorage.Homes.get(0);
        boolean foundWorld = false;

        // 寻找正确的世界
        for (ServerLevel currentWorld : Objects.requireNonNull(player.getServer()).getAllLevels()) {
            if (Objects.equals(currentWorld.dimension().location().toString(), home.world)) {
                foundWorld = true;

                BlockPos homePos = new BlockPos(home.x, home.y, home.z);
                BlockPos playerPos = new BlockPos(player.getBlockX(), player.getBlockY(), player.getBlockZ());

                if (!playerPos.equals(homePos)) {
                    player.displayClientMessage(
                        Component.literal("正在传送到家园...")
                            .withStyle(ChatFormatting.GREEN), 
                        true);
                    Teleporter(player, currentWorld, new Vec3(home.x + 0.5, home.y, home.z + 0.5));
                } else {
                    player.displayClientMessage(
                        Component.literal("你已经在家园了")
                            .withStyle(ChatFormatting.AQUA), 
                        true);
                }
                break;
            }
        }

        if (!foundWorld) {
            player.displayClientMessage(
                Component.literal("找不到家园所在的世界")
                    .withStyle(ChatFormatting.RED), 
                true);
        }
    }

    private static void DeleteHome(ServerPlayer player) throws Exception {
        StorageManager.PlayerStorageClass storages = GetPlayerStorage(player.getStringUUID());
        StorageManager.StorageClass storage = storages.storage;
        StorageManager.StorageClass.Player playerStorage = storages.playerStorage;

        if (playerStorage.Homes.isEmpty()) {
            player.displayClientMessage(
                Component.literal("你没有设置家园位置")
                    .withStyle(ChatFormatting.RED), 
                true);
            return;
        }

        playerStorage.Homes.clear();
        playerStorage.DefaultHome = "";

        StorageSaver(storage);
        player.displayClientMessage(
            Component.literal("家园位置已删除")
                .withStyle(ChatFormatting.GREEN), 
            true);
    }
}
