package dev.mrsnowy.teleport_commands.utils;

// 修改导入路径
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Vec3d;

public class TeleportUtils {
    public static void teleportPlayer(ServerPlayerEntity player, ServerWorld world, Vec3d coords) {
        // 传送前效果
        world.spawnParticles(ParticleTypes.PORTAL, player.getX(), player.getY(), player.getZ(), 15, 0.0D, 1.0D, 0.0D, 0.03);
        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 0.4f, 1.0f);

        // 传送
        player.teleport(world, coords.x, coords.y, coords.z, player.getYaw(), player.getPitch());

        // 传送后效果
        world.spawnParticles(ParticleTypes.PORTAL, player.getX(), player.getY(), player.getZ(), 15, 0.0D, 0.0D, 0.0D, 0.03);
        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 0.4f, 1.0f);
    }
}