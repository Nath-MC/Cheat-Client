package net.naxxsoftwares.mod.utils;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import static net.naxxsoftwares.mod.Initializer.client;

public abstract class PositionUtils {

    public static void lerpPositionPacket(@NotNull Vec3d from, Vec3d to, boolean serverOnly, int steps) {
        double offsetX = (to.x - from.x) / steps;
        double offsetY = (to.y - from.y) / steps;
        double offsetZ = (to.z - from.z) / steps;

        for (int i = 1; i <= steps; i++) {
            Vec3d point = from.add(offsetX * i, offsetY * i, offsetZ * i);
            updatePlayerPosition(point, serverOnly, false);
        }
    }

    public static void updatePlayerPosition(Vec3d pos, boolean serverOnly, boolean overload) {
        if (!serverOnly) client.player.setPosition(pos);
        if (overload) for (int i = 0; i < 5; i++) sendPacket(client.player.getPos()); // Note that after overloading, the server ignores all packets sent within the same tick
        sendPacket(pos); // And there's a risk that this packet is ignored too
    }

    private static void sendPacket(Vec3d pos) {
        client.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(pos.x, pos.y, pos.z, client.player.isOnGround()));
    }

}