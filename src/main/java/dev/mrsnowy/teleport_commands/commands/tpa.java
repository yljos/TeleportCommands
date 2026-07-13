package dev.mrsnowy.teleport_commands.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import dev.mrsnowy.teleport_commands.suggestions.tpaSuggestionProvider;
import dev.mrsnowy.teleport_commands.utils.TeleportUtils;
// Fix import paths
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.Formatting;

public class tpa {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // Teleport to player
        dispatcher.register(CommandManager.literal("tpa")
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .suggests(tpaSuggestionProvider.SUGGEST_PLAYERS)
                        .executes(context -> {
                            final ServerPlayerEntity targetPlayer = EntityArgumentType.getPlayer(context, "player");
                            final ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                            directTeleport(player, targetPlayer);
                            return 1;
                        }))
                // Teleport to coordinates
                .then(CommandManager.argument("x", DoubleArgumentType.doubleArg())
                        .then(CommandManager.argument("y", DoubleArgumentType.doubleArg())
                                .then(CommandManager.argument("z", DoubleArgumentType.doubleArg())
                                        .executes(context -> {
                                            final ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                            double x = DoubleArgumentType.getDouble(context, "x");
                                            double y = DoubleArgumentType.getDouble(context, "y");
                                            double z = DoubleArgumentType.getDouble(context, "z");
                                            teleportToCoordinates(player, x, y, z);
                                            return 1;
                                        })))));
    }

    private static void teleportToCoordinates(ServerPlayerEntity player, double x, double y, double z) {
        Vec3d teleportPos = new Vec3d(x, y, z);
        TeleportUtils.teleportPlayer(player, player.getServerWorld(), teleportPos);
        
        player.sendMessage(
            Text.literal(String.format("传送到: %.1f, %.1f, %.1f", x, y, z))
                .setStyle(Style.EMPTY.withColor(Formatting.GREEN)),
            false);
    }

    private static void directTeleport(ServerPlayerEntity fromPlayer, ServerPlayerEntity toPlayer) {
        if (fromPlayer == toPlayer) {
            fromPlayer.sendMessage(
                Text.literal("你不能传送到自己")
                    .setStyle(Style.EMPTY.withColor(Formatting.RED)),
                false);
            return;
        }

        TeleportUtils.teleportPlayer(fromPlayer, toPlayer.getServerWorld(), toPlayer.getPos());
        
        String targetName = toPlayer.getName().getString();
        String fromName = fromPlayer.getName().getString();
        
        fromPlayer.sendMessage(
            Text.literal("已到 " + targetName)
                .setStyle(Style.EMPTY.withColor(Formatting.GREEN)),
            false);
        toPlayer.sendMessage(
            Text.literal(fromName + " 传送到你")
                .setStyle(Style.EMPTY.withColor(Formatting.YELLOW)),
            false);
    }
}