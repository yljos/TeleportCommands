package dev.mrsnowy.teleport_commands.commands;

import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import dev.mrsnowy.teleport_commands.TeleportCommands;
import dev.mrsnowy.teleport_commands.storage.StorageManager;
import dev.mrsnowy.teleport_commands.utils.TeleportUtils;
// 修改导入路径
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.Formatting;

import java.util.Objects;

public class home {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // 设置家园位置
        dispatcher.register(CommandManager.literal("sethome")
                .executes(context -> {
                    final ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    try {
                        SetHome(player);
                        return 1;
                    } catch (Exception e) {
                        TeleportCommands.LOGGER.error("SetHome error", e);
                        player.sendMessage(Text.literal("设置家园失败")
                            .setStyle(Style.EMPTY.withColor(Formatting.RED)), false);
                        return 0;
                    }
                }));

        // 传送到家园
        dispatcher.register(CommandManager.literal("home")
                .executes(context -> {
                    final ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    try {
                        GoHome(player);
                        return 1;
                    } catch (Exception e) {
                        TeleportCommands.LOGGER.error("GoHome error", e);
                        player.sendMessage(Text.literal("传送到家园失败")
                            .setStyle(Style.EMPTY.withColor(Formatting.RED)), false);
                        return 0;
                    }
                }));
    }

    private static void SetHome(ServerPlayerEntity player) throws Exception {
        BlockPos blockPos = player.getBlockPos();
        ServerWorld world = player.getServerWorld();

        StorageManager.setPlayerHome(
            player.getUuidAsString(),
            "home",
            blockPos.getX(),
            blockPos.getY(),
            blockPos.getZ(),
            world.getRegistryKey().getValue().toString()
        );

        player.sendMessage(
            Text.literal("家园位置已设置: " + blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ())
                .setStyle(Style.EMPTY.withColor(Formatting.GREEN)), 
            false);
    }

    private static void GoHome(ServerPlayerEntity player) throws Exception {
        JsonObject home = StorageManager.getPlayerHome(player.getUuidAsString());

        if (home == null) {
            player.sendMessage(
                Text.literal("你还没有设置家园位置，使用 /sethome 来设置")
                    .setStyle(Style.EMPTY.withColor(Formatting.AQUA)), 
                false);
            return;
        }

        int homeX = home.get("x").getAsInt();
        int homeY = home.get("y").getAsInt();
        int homeZ = home.get("z").getAsInt();
        String homeWorld = home.get("world").getAsString();
        boolean foundWorld = false;

        for (ServerWorld currentWorld : Objects.requireNonNull(player.getServer()).getWorlds()) {
            if (Objects.equals(currentWorld.getRegistryKey().getValue().toString(), homeWorld)) {
                foundWorld = true;
                BlockPos homePos = new BlockPos(homeX, homeY, homeZ);
                
                if (!player.getBlockPos().equals(homePos)) {
                    player.sendMessage(
                        Text.literal("正在传送到家园...")
                            .setStyle(Style.EMPTY.withColor(Formatting.GREEN)), 
                        false);
                    TeleportUtils.teleportPlayer(player, currentWorld, new Vec3d(homeX + 0.5, homeY, homeZ + 0.5));
                } else {
                    player.sendMessage(
                        Text.literal("你已经在家园了")
                            .setStyle(Style.EMPTY.withColor(Formatting.AQUA)), 
                        false);
                }
                break;
            }
        }

        if (!foundWorld) {
            player.sendMessage(
                Text.literal("找不到家园所在的世界")
                    .setStyle(Style.EMPTY.withColor(Formatting.RED)), 
                false);
        }
    }
}