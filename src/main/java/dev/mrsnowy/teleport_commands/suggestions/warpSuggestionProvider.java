package dev.mrsnowy.teleport_commands.suggestions;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.server.command.ServerCommandSource;
import dev.mrsnowy.teleport_commands.storage.StorageManager;

import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Stream;

public class warpSuggestionProvider {
    public static final SuggestionProvider<ServerCommandSource> SUGGEST_WARPS = (context, builder) -> {
        // 获取所有 warp 文件名
        try (Stream<String> stream = Files.list(StorageManager.getDataDir())
                .filter(path -> path.getFileName().toString().startsWith("warp_") && path.getFileName().toString().endsWith(".json"))
                .map(path -> path.getFileName().toString().substring(5, path.getFileName().toString().length() - 5))) {
            stream.forEach(builder::suggest);
        } catch (IOException ignored) {}
        return builder.buildFuture();
    };
}