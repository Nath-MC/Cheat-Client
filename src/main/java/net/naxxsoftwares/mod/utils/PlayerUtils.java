package net.naxxsoftwares.mod.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.naxxsoftwares.mod.Initializer;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public abstract class PlayerUtils {

    public static final float baseTime = 1F / 20F;
    private static final MinecraftClient client = Initializer.client;

    public static boolean isEntityInReach(@NotNull Entity entity) {
        return isEntityInReach(entity, client.player.getEntityInteractionRange());
    }

    public static boolean isEntityInReach(@NotNull Entity entity, double reach) {
        return client.player.getEyePos().squaredDistanceTo(RotationsUtils.getClosestPointOnHitbox(entity)) <= Math.pow(reach, 2);
    }

    public static boolean fastIsEntityInReach(@NotNull Entity entity, double reachDistance) {
        double distanceToEntity = client.player.getEyePos().squaredDistanceTo(entity.getPos());
        double lengthOfLargestBoundingBoxSide = getLengthOfLargestBoundingBoxSide(entity.getBoundingBox());
        return distanceToEntity + Math.pow(lengthOfLargestBoundingBoxSide, 2) <= Math.pow(reachDistance, 2);
    }

    private static double getLengthOfLargestBoundingBoxSide(@NotNull Box box) {
        double[] xyzLength = {box.getLengthX(), box.getLengthY(), box.getLengthZ()};
        return Arrays.stream(xyzLength).sorted().toArray()[0];
    }

    public static boolean isCooldownFinished() {
        return client.player.getAttackCooldownProgress(baseTime) == 1F;
    }
}
