package net.naxxsoftwares.mod.utils;

import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import static net.naxxsoftwares.mod.Initializer.client;

public abstract class RotationsUtils {
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

    // This function can seem heavy, but it takes between 0.002 and 0.003 milliseconds to process
    public static @NotNull Vec3d getClosestPointOnHitbox(@NotNull Entity entity) {
        Vec3d playerEyePos = client.player.getEyePos();
        Vec3d entityPos = entity.getPos();
        Box boundingBox = entity.getBoundingBox();

        double closestXPos = 0;
        double closestYPos = 0;
        double closestZPos = 0;

        // Get bounding box dimensions
        double lengthX = truncateToTwoPlaces(boundingBox.getLengthX());
        double lengthY = truncateToTwoPlaces(boundingBox.getLengthY());
        double lengthZ = truncateToTwoPlaces(boundingBox.getLengthZ());

        // Get the number of check points along each axis
        int checkPointsX = getNumberOfCheckPoints(lengthX);
        int checkPointsY = getNumberOfCheckPoints(lengthY);
        int checkPointsZ = getNumberOfCheckPoints(lengthZ);

        double closestDistance = Double.MAX_VALUE;

        for (int i = 0; i <= checkPointsX; i++) {
            for (int j = 0; j <= checkPointsY; j++) {
                for (int k = 0; k <= checkPointsZ; k++) {
                    // Calculate offsets for the current point
                    double offsetX = (lengthX / checkPointsX) * i;
                    double offsetY = (lengthY / checkPointsY) * j;
                    double offsetZ = (lengthZ / checkPointsZ) * k;

                    Vec3d point = entityPos.add(offsetX, offsetY, offsetZ);
                    double distance = playerEyePos.squaredDistanceTo(point);

                    // Update the closest point if the current one is closer
                    if (distance < closestDistance) {
                        closestDistance = distance;
                        closestXPos = point.x;
                        closestYPos = point.y;
                        closestZPos = point.z;
                    }
                }
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

    public static void setServerYaw(float serverYaw) {
        RotationsUtils.serverYaw = serverYaw;
    }

    public static void setServerPitch(float serverPitch) {
        RotationsUtils.serverPitch = serverPitch;
    }

    public static void reset() {
        setServerYaw(client.player.getYaw());
        setServerPitch(client.player.getPitch());
    }

    public static void applyRotation(boolean serverOnly) {
        if (!serverOnly) client.player.setAngles(serverYaw, serverPitch);
        client.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(serverYaw, serverPitch, client.player.isOnGround()));
    }
}
