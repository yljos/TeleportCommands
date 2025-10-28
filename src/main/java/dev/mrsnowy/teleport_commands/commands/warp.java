package dev.mrsnowy.teleport_commands.commands;

import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.mrsnowy.teleport_commands.storage.StorageManager;
import dev.mrsnowy.teleport_commands.utils.TeleportUtils;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class warp {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // /setwarp <name>
        dispatcher.register(CommandManager.literal("setwarp")
            .then(CommandManager.argument("name", StringArgumentType.word())
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    String name = StringArgumentType.getString(context, "name");
                    try {
                        setWarp(player, name);
                        return 1;
                    } catch (Exception e) {
                        player.sendMessage(Text.literal("设置失败").setStyle(Style.EMPTY.withColor(Formatting.RED)), false);
                        return 0;
                    }
                })));

        // /warp <name>  (带补全)
        dispatcher.register(CommandManager.literal("warp")
            .then(CommandManager.argument("name", StringArgumentType.word())
                .suggests(warp::suggestions)
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    String name = StringArgumentType.getString(context, "name");
                    try {
                        doWarp(player, name);
                        return 1;
                    } catch (Exception e) {
                        player.sendMessage(Text.literal("传送失败").setStyle(Style.EMPTY.withColor(Formatting.RED)), false);
                        return 0;
                    }
                })));

        // /delwarp <name> (带补全)
        dispatcher.register(CommandManager.literal("delwarp")
            .then(CommandManager.argument("name", StringArgumentType.word())
                .suggests(warp::suggestions)
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    String name = StringArgumentType.getString(context, "name");
                    try {
                        boolean ok = StorageManager.delWarp(name);
                        if (ok) {
                            player.sendMessage(Text.literal("已删除 " + name).setStyle(Style.EMPTY.withColor(Formatting.GREEN)), false);
                            return 1;
                        } else {
                            player.sendMessage(Text.literal("未找到 " + name).setStyle(Style.EMPTY.withColor(Formatting.AQUA)), false);
                            return 0;
                        }
                    } catch (Exception e) {
                        player.sendMessage(Text.literal("删除失败").setStyle(Style.EMPTY.withColor(Formatting.RED)), false);
                        return 0;
                    }
                })));
    }

    private static void setWarp(ServerPlayerEntity player, String name) {
        BlockPos pos = player.getBlockPos();
        ServerWorld world = player.getServerWorld();
        StorageManager.setWarp(name, pos.getX(), pos.getY(), pos.getZ(), world.getRegistryKey().getValue().toString());

        player.sendMessage(
            Text.literal("已设置 " + name).setStyle(Style.EMPTY.withColor(Formatting.GREEN)),
            false
        );
    }

    private static void doWarp(ServerPlayerEntity player, String name) {
        JsonObject warp = StorageManager.getWarp(name);
        if (warp == null) {
            player.sendMessage(Text.literal("未找到 " + name).setStyle(Style.EMPTY.withColor(Formatting.AQUA)), false);
            return;
        }

        int x = warp.get("x").getAsInt();
        int y = warp.get("y").getAsInt();
        int z = warp.get("z").getAsInt();
        String worldStr = warp.get("world").getAsString();

        boolean foundWorld = false;
        for (ServerWorld currentWorld : Objects.requireNonNull(player.getServer()).getWorlds()) {
            if (Objects.equals(currentWorld.getRegistryKey().getValue().toString(), worldStr)) {
                foundWorld = true;
                BlockPos targetPos = new BlockPos(x, y, z);
                if (!player.getBlockPos().equals(targetPos)) {
                    player.sendMessage(Text.literal("传送至 " + name).setStyle(Style.EMPTY.withColor(Formatting.GREEN)), false);
                    TeleportUtils.teleportPlayer(player, currentWorld, new Vec3d(x + 0.5, y, z + 0.5));
                } else {
                    player.sendMessage(Text.literal("已在 " + name).setStyle(Style.EMPTY.withColor(Formatting.AQUA)), false);
                }
                break;
            }
        }

        if (!foundWorld) {
            player.sendMessage(Text.literal("找不到 " + name + " 的世界").setStyle(Style.EMPTY.withColor(Formatting.RED)), false);
        }
    }

    // 简单补全：从 StorageManager.listWarpNames() 读取并建议
    public static CompletableFuture<Suggestions> suggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        try {
            Set<String> names = StorageManager.listWarpNames();
            for (String n : names) {
                builder.suggest(n);
            }
        } catch (Exception ignored) {
        }
        return builder.buildFuture();
    }
}