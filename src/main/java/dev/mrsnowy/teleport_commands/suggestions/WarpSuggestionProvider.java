package dev.mrsnowy.teleport_commands.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.mrsnowy.teleport_commands.TeleportCommands;
import dev.mrsnowy.teleport_commands.storage.StorageManager;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import static dev.mrsnowy.teleport_commands.storage.StorageManager.GetPlayerStorage;

public class WarpSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            StorageManager.PlayerStorageClass storages = GetPlayerStorage(player.getStringUUID());
            StorageManager.StorageClass storage = storages.storage;

            if (storage.Warps != null) {
                for (StorageManager.StorageClass.Warp currentWarp : storage.Warps) {
                    builder.suggest(currentWarp.name);
                }
            }

            // Build and return the suggestions
            return builder.buildFuture();
        } catch (Exception e) {
            TeleportCommands.LOGGER.error("Error getting warp suggestions!");
            return null;
        }
    }
}
