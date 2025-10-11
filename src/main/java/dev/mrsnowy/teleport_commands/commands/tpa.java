package dev.mrsnowy.teleport_commands.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import dev.mrsnowy.teleport_commands.suggestions.tpaSuggestionProvider;
import dev.mrsnowy.teleport_commands.utils.TeleportUtils;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

public class tpa {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // 传送到玩家
        dispatcher.register(CommandManager.literal("tpa")
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .suggests(tpaSuggestionProvider.SUGGEST_PLAYERS)
                        .executes(context -> {
                            final ServerPlayerEntity targetPlayer = EntityArgumentType.getPlayer(context, "player");
                            final ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                            handleTpa(player, targetPlayer);
                            return 1;
                        }))
                // 坐标传送
                .then(CommandManager.argument("x", DoubleArgumentType.doubleArg())
                        .then(CommandManager.argument("y", DoubleArgumentType.doubleArg())
                                .then(CommandManager.argument("z", DoubleArgumentType.doubleArg())
                                        .executes(context -> {
                                            final ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                            double x = DoubleArgumentType.getDouble(context, "x");
                                            double y = DoubleArgumentType.getDouble(context, "y");
                                            double z = DoubleArgumentType.getDouble(context, "z");
                                            TeleportUtils.createTeleport(player, player.getServerWorld(), new Vec3d(x, y, z),
                                                    null,
                                                    Text.literal(String.format("传送到: %.1f, %.1f, %.1f", x, y, z))
                                                            .setStyle(Style.EMPTY.withColor(Formatting.GREEN)));
                                            return 1;
                                        })))));

        // 将玩家传送到自己
        dispatcher.register(CommandManager.literal("tpahere")
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .suggests(tpaSuggestionProvider.SUGGEST_PLAYERS)
                        .executes(context -> {
                            final ServerPlayerEntity targetPlayer = EntityArgumentType.getPlayer(context, "player");
                            final ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                            handleTpaHere(player, targetPlayer);
                            return 1;
                        })));
    }

    private static void handleTpa(ServerPlayerEntity player, ServerPlayerEntity targetPlayer) {
        if (player == targetPlayer) {
            player.sendMessage(
                    Text.literal("你不能传送到自己").setStyle(Style.EMPTY.withColor(Formatting.RED)),
                    false);
            return;
        }

        TeleportUtils.createTeleport(
                player,
                targetPlayer.getServerWorld(),
                targetPlayer.getBlockPos().toCenterPos(),
                null,
                Text.literal("已到 " + targetPlayer.getName().getString()).setStyle(Style.EMPTY.withColor(Formatting.GREEN))
        );

        targetPlayer.sendMessage(
                Text.literal(player.getName().getString() + " 传送到你").setStyle(Style.EMPTY.withColor(Formatting.YELLOW)),
                false
        );
    }

    private static void handleTpaHere(ServerPlayerEntity issuer, ServerPlayerEntity targetPlayer) {
        if (issuer == targetPlayer) {
            issuer.sendMessage(
                    Text.literal("你不能将自己传送给自己").setStyle(Style.EMPTY.withColor(Formatting.RED)),
                    false);
            return;
        }

        TeleportUtils.createTeleport(
                targetPlayer,
                issuer.getServerWorld(),
                issuer.getBlockPos().toCenterPos(),
                null,
                Text.literal("你被 " + issuer.getName().getString() + " 传送了").setStyle(Style.EMPTY.withColor(Formatting.YELLOW))
        );

        issuer.sendMessage(
                Text.literal("已将 " + targetPlayer.getName().getString() + " 拉来").setStyle(Style.EMPTY.withColor(Formatting.GREEN)),
                false
        );
    }
}
