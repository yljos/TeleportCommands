package dev.mrsnowy.teleport_commands.commands;

import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import dev.mrsnowy.teleport_commands.TeleportCommands;
import dev.mrsnowy.teleport_commands.storage.StorageManager;
import dev.mrsnowy.teleport_commands.utils.TeleportUtils;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

public class home {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // 设置家
        dispatcher.register(CommandManager.literal("sethome")
                .executes(context -> {
                    final ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    try {
                        SetHome(player);
                        return 1;
                    } catch (Exception e) {
                        TeleportCommands.LOGGER.error("SetHome error", e);
                        player.sendMessage(Text.literal("设置家失败")
                                .setStyle(Style.EMPTY.withColor(Formatting.RED)), false);
                        return 0;
                    }
                }));

        // 传送回家
        dispatcher.register(CommandManager.literal("home")
                .executes(context -> {
                    final ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    try {
                        GoHome(player);
                        return 1;
                    } catch (Exception e) {
                        TeleportCommands.LOGGER.error("GoHome error", e);
                        player.sendMessage(Text.literal("传送到家失败")
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
                blockPos.getX(),
                blockPos.getY(),
                blockPos.getZ(),
                world.getRegistryKey().getValue().toString()
        );

        player.sendMessage(
                Text.literal("家已设置: " + blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ())
                        .setStyle(Style.EMPTY.withColor(Formatting.GREEN)),
                false);
    }

    private static void GoHome(ServerPlayerEntity player) throws Exception {
        JsonObject homeData = StorageManager.getPlayerHome(player.getUuidAsString());

        if (homeData == null) {
            player.sendMessage(
                    Text.literal("你还没有设置家，使用 /sethome 来设置")
                            .setStyle(Style.EMPTY.withColor(Formatting.AQUA)),
                    false);
            return;
        }

        int homeX = homeData.get("x").getAsInt();
        int homeY = homeData.get("y").getAsInt();
        int homeZ = homeData.get("z").getAsInt();
        String homeWorldId = homeData.get("world").getAsString();

        ServerWorld targetWorld = null;
        for (ServerWorld world : player.getServer().getWorlds()) {
            if (Objects.equals(world.getRegistryKey().getValue().toString(), homeWorldId)) {
                targetWorld = world;
                break;
            }
        }

        if (targetWorld == null) {
            player.sendMessage(
                    Text.literal("找不到家园所在的世界")
                            .setStyle(Style.EMPTY.withColor(Formatting.RED)),
                    false);
            return;
        }

        BlockPos homePos = new BlockPos(homeX, homeY, homeZ);
        if (player.getBlockPos().equals(homePos) && player.getServerWorld() == targetWorld) {
            player.sendMessage(
                    Text.literal("已经在家")
                            .setStyle(Style.EMPTY.withColor(Formatting.AQUA)),
                    false);
            return;
        }

        TeleportUtils.createTeleport(
                player,
                targetWorld,
                new Vec3d(homeX + 0.5, homeY, homeZ + 0.5),
                Text.literal("正在回家...").setStyle(Style.EMPTY.withColor(Formatting.GREEN)),
                null
        );
    }
}