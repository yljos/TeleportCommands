package dev.mrsnowy.teleport_commands.utils;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;

public class TeleportUtils {
    public static void teleportPlayer(ServerPlayerEntity player, ServerWorld world, Vec3d coords) {
        // 传送前音效 - 使用简化的方法
        player.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 2.0f, 1.0f);

        // 使用 TeleportTarget 进行传送
        if (player.getServerWorld() == world) {
            // 同世界传送
            player.requestTeleport(coords.x, coords.y, coords.z);
        } else {
            // 跨维度传送
            TeleportTarget target = new TeleportTarget(
                world, 
                coords, 
                Vec3d.ZERO, 
                player.getYaw(), 
                player.getPitch(),
                (entity) -> {}
            );
            player.teleportTo(target);
        }

        // 传送后音效 - 使用简化的方法
        player.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 2.0f, 1.0f);
    }
}
