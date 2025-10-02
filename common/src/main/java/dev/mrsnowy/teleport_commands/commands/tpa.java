package dev.mrsnowy.teleport_commands.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.datafixers.util.Pair;
import dev.mrsnowy.teleport_commands.suggestions.tpaSuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

import static dev.mrsnowy.teleport_commands.utils.tools.*;

public class tpa {

    public static void register(Commands commandManager) {

        // 传送到玩家
        commandManager.getDispatcher().register(Commands.literal("tpa")
                .then(Commands.argument("player", EntityArgument.player())
                        .suggests(tpaSuggestionProvider.SUGGEST_PLAYERS)
                        .executes(context -> {
                            final ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "player");
                            final ServerPlayer player = context.getSource().getPlayerOrException();

                            directTeleport(player, targetPlayer, false);
                            return 1;
                        }))
                // 添加 x y z 坐标传送
                .then(Commands.argument("x", DoubleArgumentType.doubleArg())
                        .then(Commands.argument("y", DoubleArgumentType.doubleArg())
                                .then(Commands.argument("z", DoubleArgumentType.doubleArg())
                                        .executes(context -> {
                                            final ServerPlayer player = context.getSource().getPlayerOrException();
                                            double x = DoubleArgumentType.getDouble(context, "x");
                                            double y = DoubleArgumentType.getDouble(context, "y");
                                            double z = DoubleArgumentType.getDouble(context, "z");

                                            teleportToCoordinates(player, x, y, z);
                                            return 1;
                                        })))));
        // 传送玩家到自己这里
        commandManager.getDispatcher().register(Commands.literal("tpahere")
                .then(Commands.argument("player", EntityArgument.player())
                        .suggests(tpaSuggestionProvider.SUGGEST_PLAYERS)
                        .executes(context -> {
                            final ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "player");
                            final ServerPlayer player = context.getSource().getPlayerOrException();

                            directTeleport(player, targetPlayer, true);
                            return 1;
                        })));
    }

    private static void teleportToCoordinates(ServerPlayer player, double x, double y, double z) {
        Vec3 targetPos = new Vec3(x, y, z);
        
        // 检查传送安全性
        Pair<Integer, Optional<Vec3>> teleportData = teleportSafetyChecker(
            (int) x, (int) y, (int) z, 
            player.serverLevel(), 
            player
        );

        Vec3 teleportPos = null;
        
        switch (teleportData.getFirst()) {
            case 0: // 安全位置
            case 1: // 相同位置
                if (teleportData.getSecond().isPresent()) {
                    teleportPos = teleportData.getSecond().get();
                } else {
                    teleportPos = targetPos; // 如果没有安全位置，直接传送到目标位置
                }
                break;
            case 2: // 无安全位置，直接传送到目标位置
                teleportPos = targetPos;
                break;
        }

        // 执行传送
        if (teleportPos != null) {
            Teleporter(player, player.serverLevel(), teleportPos);
            
            player.displayClientMessage(
                Component.literal(String.format("传送到: %.1f, %.1f, %.1f", 
                    teleportPos.x, teleportPos.y, teleportPos.z))
                    .withStyle(ChatFormatting.GREEN), 
                true);
        }
    }

    private static void directTeleport(ServerPlayer fromPlayer, ServerPlayer toPlayer, boolean here) {
        if (fromPlayer == toPlayer) {
            fromPlayer.displayClientMessage(
                getTranslatedText("commands.teleport_commands.tpa.self", fromPlayer)
                    .withStyle(ChatFormatting.RED), 
                true);
            return;
        }

        ServerPlayer destinationPlayer = here ? fromPlayer : toPlayer;
        ServerPlayer teleportingPlayer = here ? toPlayer : fromPlayer;

        // 检查传送安全性
        Pair<Integer, Optional<Vec3>> teleportData = teleportSafetyChecker(
            destinationPlayer.getBlockX(), 
            destinationPlayer.getBlockY(), 
            destinationPlayer.getBlockZ(), 
            destinationPlayer.serverLevel(), 
            teleportingPlayer
        );

        Vec3 teleportPos = null;
        
        switch (teleportData.getFirst()) {
            case 0: // 安全位置
            case 1: // 相同位置
                if (teleportData.getSecond().isPresent()) {
                    teleportPos = teleportData.getSecond().get();
                } else {
                    teleportingPlayer.displayClientMessage(
                        getTranslatedText("commands.teleport_commands.common.error", teleportingPlayer)
                            .withStyle(ChatFormatting.RED, ChatFormatting.BOLD), 
                        true);
                    return;
                }
                break;
            case 2: // 无安全位置，直接传送到玩家位置
                teleportPos = destinationPlayer.position();
                break;
        }

        // 执行传送
        if (teleportPos != null) {
            Teleporter(teleportingPlayer, destinationPlayer.serverLevel(), teleportPos);
            
            // 发送成功消息
            String targetName = toPlayer.getName().getString();
            String fromName = fromPlayer.getName().getString();
            
            if (here) {
                fromPlayer.displayClientMessage(
                    Component.literal("已将 " + targetName + " 传送到你这里")
                        .withStyle(ChatFormatting.GREEN), 
                    true);
                toPlayer.displayClientMessage(
                    Component.literal("你被 " + fromName + " 传送了")
                        .withStyle(ChatFormatting.YELLOW), 
                    true);
            } else {
                fromPlayer.displayClientMessage(
                    Component.literal("已传送到 " + targetName)
                        .withStyle(ChatFormatting.GREEN), 
                    true);
                toPlayer.displayClientMessage(
                    Component.literal(fromName + " 传送到了你这里")
                        .withStyle(ChatFormatting.YELLOW), 
                    true);
            }
        }
    }
}
