package dev.mrsnowy.teleport_commands.suggestions;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.server.command.ServerCommandSource;

public class tpaSuggestionProvider {
    public static final SuggestionProvider<ServerCommandSource> SUGGEST_PLAYERS = (context, builder) -> {
        // 获取在线玩家列表并提供建议
        context.getSource().getServer().getPlayerManager().getPlayerList().forEach(player -> {
            if (player != context.getSource().getEntity()) {
                builder.suggest(player.getName().getString());
            }
        });
        return builder.buildFuture();
    };
}