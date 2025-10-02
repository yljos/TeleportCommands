package dev.mrsnowy.teleport_commands.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.CompletableFuture;

public class tpaSuggestionProvider {
    
    public static final SuggestionProvider<CommandSourceStack> SUGGEST_PLAYERS = (context, builder) -> {
        // 获取在线玩家列表并提供建议
        context.getSource().getServer().getPlayerList().getPlayers().forEach(player -> {
            if (player != context.getSource().getEntity()) { // 不包括自己
                builder.suggest(player.getName().getString());
            }
        });
        return builder.buildFuture();
    };
}
