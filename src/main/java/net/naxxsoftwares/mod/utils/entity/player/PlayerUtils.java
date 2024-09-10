package net.naxxsoftwares.mod.utils.entity.player;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.naxxsoftwares.mod.Initializer;
import org.jetbrains.annotations.NotNull;

public abstract class PlayerUtils {

    public static final float baseTime = 1F / 20F; //
    private static final MinecraftClient client = Initializer.client;

    public static boolean isEntityInReach(@NotNull Entity entity) {
        return isEntityInReach(entity, client.player.getEntityInteractionRange());
    }

    public static boolean isEntityInReach(@NotNull Entity entity, double reach) {
        return client.player.getEyePos().squaredDistanceTo(RotationsUtils.getClosestPointOnHitbox(entity)) <= Math.pow(reach, 2);
    }

    public static boolean isCooldownFinished() {
        return client.player.getAttackCooldownProgress(baseTime) == 1F;
    }
}
