package net.naxxsoftwares.mod.utils.entity.player;

import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import static net.naxxsoftwares.mod.Initializer.client;

public class RotationsUtils {
    public static float serverYaw;
    public static float serverPitch;

    public static void setRotationOn(Entity entity) {
        Vec3d targetPos = getClosestPointOnHitbox(entity);
        Vec3d playerEyePos = client.player.getEyePos();

        double d = targetPos.x - playerEyePos.x;
        double e = targetPos.y - playerEyePos.y;
        double f = targetPos.z - playerEyePos.z;
        double g = Math.sqrt(d * d + f * f);

        serverYaw = MathHelper.wrapDegrees((float) (MathHelper.atan2(f, d) * 180.0F / Math.PI) - 90.0F);
        serverPitch = MathHelper.wrapDegrees((float) (-(MathHelper.atan2(e, g) * 180.0F / Math.PI)));
    }

    public static @NotNull Vec3d getClosestPointOnHitbox(@NotNull Entity entity) {
        Vec3d playerEyePos = client.player.getEyePos();
        Vec3d entityPos = entity.getPos();

        double closestXPos = 0;
        double closestYPos = 0;
        double closestZPos = 0;

        // Truncates to the last two decimal digits
        double lengthX = truncateToTwoPlaces(entity.getBoundingBox().getLengthX());
        double lengthY = truncateToTwoPlaces(entity.getBoundingBox().getLengthY());
        double lengthZ = truncateToTwoPlaces(entity.getBoundingBox().getLengthZ());

        int checkPointsX = getNumberOfCheckPoints(lengthX);
        int checkPointsY = getNumberOfCheckPoints(lengthY); // For lengthY = 1.95, checkPointsY = 19.5
        int checkPointsZ = getNumberOfCheckPoints(lengthZ);

        double closestDistance = Double.MAX_VALUE;

        for (int i = 0; i <= checkPointsX; i++) {
            double offset = (lengthX / checkPointsX) * i;
            Vec3d point = entityPos.add(offset, 0, 0);
            double distance = playerEyePos.squaredDistanceTo(point);

            if (distance < closestDistance) {
                closestDistance = distance;
                closestXPos = point.x;
            }
        }

        closestDistance = Double.MAX_VALUE; // Reset the value

        for (int i = 0; i <= checkPointsY; i++) {
            double offset = (lengthY / checkPointsY) * i;
            Vec3d point = entityPos.add(0, offset, 0);
            double distance = playerEyePos.squaredDistanceTo(point);

            if (distance < closestDistance) {
                closestDistance = distance;
                closestYPos = point.y;
            }
        }

        closestDistance = Double.MAX_VALUE; // Reset the value

        for (int i = 0; i <= checkPointsZ; i++) {
            double offset = (lengthZ / checkPointsZ) * i;
            Vec3d point = entityPos.add(0, 0, offset);
            double distance = playerEyePos.squaredDistanceTo(point);

            if (distance < closestDistance) {
                closestDistance = distance;
                closestZPos = point.z;
            }
        }

        return new Vec3d(closestXPos, closestYPos, closestZPos);
    }

    private static double truncateToTwoPlaces(double value) {
        return (double) (int) (value * 100) / 100;
    }

    private static int getNumberOfCheckPoints(double length) {
        int truncatedValue = ((int) (length * 100)) / 10;
        return truncatedValue >= 8 ? truncatedValue : (int) (truncatedValue * 1.5);
    }

    public static float getServerYaw() {
        return serverYaw;
    }

    public static float getServerPitch() {
        return serverPitch;
    }

    public static void applyRotation(boolean serverOnly) {
        if (!serverOnly) client.player.setAngles(serverYaw, serverPitch);
        client.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(serverYaw, serverPitch, client.player.isOnGround()));
    }
}
