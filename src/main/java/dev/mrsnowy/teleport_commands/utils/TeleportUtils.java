package dev.mrsnowy.teleport_commands.utils;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;

public class TeleportUtils {
    public static void teleportPlayer(ServerPlayerEntity player, ServerWorld world, Vec3d coords) {
        

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

       
        
    }
}
