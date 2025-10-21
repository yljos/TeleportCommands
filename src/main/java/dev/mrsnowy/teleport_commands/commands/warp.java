package dev.mrsnowy.teleport_commands.commands;

import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
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

public class warp {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // 设置传送点
        dispatcher.register(CommandManager.literal("setwarp")
            .then(CommandManager.argument("name", StringArgumentType.word())
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    String name = StringArgumentType.getString(context, "name");
                    BlockPos pos = player.getBlockPos();
                    ServerWorld world = player.getServerWorld();
                    StorageManager.setWarp(name, pos.getX(), pos.getY(), pos.getZ(), world.getRegistryKey().getValue().toString());
                    player.sendMessage(Text.literal("已设置传送点: " + name)
                        .setStyle(Style.EMPTY.withColor(Formatting.GREEN)), false);
                    return 1;
                })
            )
        );

        // 传送到传送点
        dispatcher.register(CommandManager.literal("warp")
            .then(CommandManager.argument("name", StringArgumentType.word())
                .suggests(dev.mrsnowy.teleport_commands.suggestions.warpSuggestionProvider.SUGGEST_WARPS)
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    String name = StringArgumentType.getString(context, "name");
                    JsonObject warp = StorageManager.getWarp(name);
                    if (warp == null) {
                        player.sendMessage(Text.literal("传送点不存在: " + name)
                            .setStyle(Style.EMPTY.withColor(Formatting.RED)), false);
                        return 0;
                    }
                    int x = warp.get("x").getAsInt();
                    int y = warp.get("y").getAsInt();
                    int z = warp.get("z").getAsInt();
                    String worldId = warp.get("world").getAsString();

                    ServerWorld targetWorld = null;
                    // 修复：遍历世界时用 context.getSource().getServer().getWorlds()
                    for (ServerWorld w : context.getSource().getServer().getWorlds()) {
                        if (Objects.equals(w.getRegistryKey().getValue().toString(), worldId)) {
                            targetWorld = w;
                            break;
                        }
                    }
                    if (targetWorld == null) {
                        player.sendMessage(Text.literal("找不到传送点所在的世界")
                            .setStyle(Style.EMPTY.withColor(Formatting.RED)), false);
                        return 0;
                    }
                    TeleportUtils.createTeleport(
                        player,
                        targetWorld,
                        new Vec3d(x + 0.5, y, z + 0.5),
                        null,
                        Text.literal("已传送到: " + name).setStyle(Style.EMPTY.withColor(Formatting.GREEN))
                    );
                    return 1;
                })
            )
        );
    }
}