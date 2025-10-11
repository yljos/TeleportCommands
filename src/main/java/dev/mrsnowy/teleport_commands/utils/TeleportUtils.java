package dev.mrsnowy.teleport_commands.utils;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;

public class TeleportUtils {

    /**
     * 统一的传送方法，处理传送、消息和音效。
     * 这是执行传送操作的唯一公共入口点。
     * @param player 传送的玩家
     * @param world 目标世界
     * @param coords 目标坐标
     * @param preTeleportMessage 传送前的消息 (可为 null)
     * @param postTeleportMessage 传送后的消息 (可为 null)
     */
    public static void createTeleport(
            ServerPlayerEntity player,
            ServerWorld world,
            Vec3d coords,
            Text preTeleportMessage,
            Text postTeleportMessage
    ) {
        if (preTeleportMessage != null) {
            player.sendMessage(preTeleportMessage, false);
        }

        // 调用私有的底层传送逻辑
        teleportPlayerInternal(player, world, coords);
        player.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 2.0f, 1.0f);

        if (postTeleportMessage != null) {
            player.sendMessage(postTeleportMessage, false);
        }
    }

    /**
     * 底层的传送实现方法，处理同世界和跨维度传送。
     * 该方法被设为私有，以确保所有传送都通过 createTeleport 调用。
     * @param player 玩家
     * @param world 目标世界
     * @param coords 目标坐标
     */
    private static void teleportPlayerInternal(ServerPlayerEntity player, ServerWorld world, Vec3d coords) {
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