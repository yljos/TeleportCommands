package dev.mrsnowy.teleport_commands.commands;

import dev.mrsnowy.teleport_commands.TeleportCommands;
import dev.mrsnowy.teleport_commands.storage.StorageManager;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import static dev.mrsnowy.teleport_commands.utils.tools.getTranslatedText;
import static dev.mrsnowy.teleport_commands.storage.StorageManager.GetPlayerStorage;
import static dev.mrsnowy.teleport_commands.storage.StorageManager.StorageSaver;
import static dev.mrsnowy.teleport_commands.utils.tools.Teleporter;

public class home {
    public static void register(Commands commandManager) {

        // /sethome - sets the player's single home
        commandManager.getDispatcher().register(Commands.literal("sethome")
                .executes(context -> {
                    final ServerPlayer player = context.getSource().getPlayerOrException();

                    try {
                        SetHome(player);
                    } catch (Exception e) {
                        TeleportCommands.LOGGER.error(String.valueOf(e));
                        player.displayClientMessage(getTranslatedText("commands.teleport_commands.home.setError", player).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
                        return 1;
                    }
                    return 0;
                }));

        // /home - teleports to player's home
        commandManager.getDispatcher().register(Commands.literal("home")
                .executes(context -> {
                    final ServerPlayer player = context.getSource().getPlayerOrException();

                    try {
                        GoHome(player);
                    } catch (Exception e) {
                        TeleportCommands.LOGGER.error(String.valueOf(e));
                        player.displayClientMessage(getTranslatedText("commands.teleport_commands.home.goError", player).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
                        return 1;
                    }
                    return 0;
                }));

        // /delhome - deletes player's home
        commandManager.getDispatcher().register(Commands.literal("delhome")
                .executes(context -> {
                    final ServerPlayer player = context.getSource().getPlayerOrException();

                    try {
                        DeleteHome(player);
                    } catch (Exception e) {
                        TeleportCommands.LOGGER.error(String.valueOf(e));
                        player.displayClientMessage(getTranslatedText("commands.teleport_commands.home.deleteError", player).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
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

        // Clear existing homes and set the single home
        playerStorage.Homes.clear();
        
        StorageManager.StorageClass.Player.Home homeLocation = new StorageManager.StorageClass.Player.Home();
        homeLocation.name = "home";
        homeLocation.x = blockPos.getX();
        homeLocation.y = blockPos.getY();
        homeLocation.z = blockPos.getZ();
        homeLocation.world = world.dimension().location().toString();

        playerStorage.Homes.add(homeLocation);
        playerStorage.DefaultHome = "home";

        StorageSaver(storage);
        player.displayClientMessage(getTranslatedText("commands.teleport_commands.home.set", player), true);
    }

    private static void GoHome(ServerPlayer player) throws Exception {
        StorageManager.StorageClass.Player playerStorage = GetPlayerStorage(player.getStringUUID()).playerStorage;

        if (playerStorage.Homes.isEmpty()) {
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.home.homeless", player).withStyle(ChatFormatting.AQUA), true);
            return;
        }

        // Get the first (and only) home
        StorageManager.StorageClass.Player.Home home = playerStorage.Homes.get(0);
        
        // Find the world
        ServerLevel homeWorld = null;
        for (ServerLevel currentWorld : Objects.requireNonNull(player.getServer()).getAllLevels()) {
            if (Objects.equals(currentWorld.dimension().location().toString(), home.world)) {
                homeWorld = currentWorld;
                break;
            }
        }
        
        if (homeWorld == null) {
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.common.error", player).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
            return;
        }
        
        BlockPos homePos = new BlockPos(home.x, home.y, home.z);
        
        // Check if player is already at home location
        if (player.blockPosition().equals(homePos) && player.level() == homeWorld) {
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.home.goSame", player).withStyle(ChatFormatting.AQUA), true);
        } else {
            Vec3 teleportPos = new Vec3(home.x + 0.5, home.y, home.z + 0.5);
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.home.go", player), true);
            Teleporter(player, homeWorld, teleportPos);
        }
    }

    private static void DeleteHome(ServerPlayer player) throws Exception {
        StorageManager.PlayerStorageClass storages = GetPlayerStorage(player.getStringUUID());
        StorageManager.StorageClass storage = storages.storage;
        StorageManager.StorageClass.Player playerStorage = storages.playerStorage;

        if (playerStorage.Homes.isEmpty()) {
            player.displayClientMessage(getTranslatedText("commands.teleport_commands.home.homeless", player).withStyle(ChatFormatting.AQUA), true);
            return;
        }

        playerStorage.Homes.clear();
        playerStorage.DefaultHome = "";

        StorageSaver(storage);
        player.displayClientMessage(getTranslatedText("commands.teleport_commands.home.delete", player), true);
    }
}
