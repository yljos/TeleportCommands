package dev.mrsnowy.teleport_commands.commands;

import com.mojang.datafixers.util.Pair;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.Optional;

import static dev.mrsnowy.teleport_commands.utils.tools.*;

public class tpa {

    public static void register(Commands commandManager) {

        commandManager.getDispatcher().register(Commands.literal("tpa")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> {
                            final ServerPlayer TargetPlayer = EntityArgument.getPlayer(context, "player");
                            final ServerPlayer player = context.getSource().getPlayerOrException();

                            tpaCommandHandler(player, TargetPlayer, false);
                            return 0;
                        })));

        commandManager.getDispatcher().register(Commands.literal("tpahere")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> {
                            final ServerPlayer TargetPlayer = EntityArgument.getPlayer(context, "player");
                            final ServerPlayer player = context.getSource().getPlayerOrException();

                            tpaCommandHandler(player, TargetPlayer, true);
                            return 0;
                        })));
    }



    private static void tpaCommandHandler(ServerPlayer FromPlayer, ServerPlayer ToPlayer, boolean here) throws NullPointerException {
        if (FromPlayer == ToPlayer) {
            FromPlayer.displayClientMessage(getTranslatedText("commands.teleport_commands.tpa.self", FromPlayer).withStyle(ChatFormatting.AQUA), true);
            return;
        }

        // Determine who teleports to whom
        ServerPlayer destinationPlayer = here ? FromPlayer : ToPlayer;
        ServerPlayer teleportingPlayer = here ? ToPlayer : FromPlayer;
        
        String hereText = here ? "Here" : "";
        String destinationPlayerName = Objects.requireNonNull(destinationPlayer.getName().getString());
        String teleportingPlayerName = Objects.requireNonNull(teleportingPlayer.getName().getString());

        // Safety check for teleportation
        Pair<Integer, Optional<Vec3>> teleportData = teleportSafetyChecker(
            destinationPlayer.getBlockX(), 
            destinationPlayer.getBlockY(), 
            destinationPlayer.getBlockZ(), 
            destinationPlayer.serverLevel(), 
            teleportingPlayer
        );

        switch (teleportData.getFirst()) {
            case 1: // same location
                teleportingPlayer.displayClientMessage(
                    getTranslatedText("commands.teleport_commands.home.goSame", teleportingPlayer).withStyle(ChatFormatting.AQUA), 
                    true
                );
                return;
            case 0: // safe location found
                if (teleportData.getSecond().isPresent()) {
                    Teleporter(teleportingPlayer, destinationPlayer.serverLevel(), teleportData.getSecond().get());
                    break;
                } else {
                    teleportingPlayer.displayClientMessage(
                        getTranslatedText("commands.teleport_commands.common.error", teleportingPlayer).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), 
                        true
                    );
                    return;
                }
            case 2: // no safe location, teleport anyway
                Teleporter(teleportingPlayer, destinationPlayer.serverLevel(), destinationPlayer.position());
                break;
        }

        // Success messages
        if (here) {
            FromPlayer.displayClientMessage(
                getTranslatedText("commands.teleport_commands.common.teleport", FromPlayer, 
                Component.literal(teleportingPlayerName).withStyle(ChatFormatting.BOLD)).withStyle(ChatFormatting.GREEN), 
                true
            );
            ToPlayer.displayClientMessage(
                getTranslatedText("commands.teleport_commands.common.teleport", ToPlayer).withStyle(ChatFormatting.GREEN), 
                true
            );
        } else {
            FromPlayer.displayClientMessage(
                getTranslatedText("commands.teleport_commands.common.teleport", FromPlayer).withStyle(ChatFormatting.GREEN), 
                true
            );
            ToPlayer.displayClientMessage(
                getTranslatedText("commands.teleport_commands.common.teleport", ToPlayer, 
                Component.literal(FromPlayer.getName().getString()).withStyle(ChatFormatting.BOLD)).withStyle(ChatFormatting.WHITE), 
                true
            );
        }
    }
}
