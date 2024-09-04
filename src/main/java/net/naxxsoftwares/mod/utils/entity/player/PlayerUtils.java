package net.naxxsoftwares.mod.utils.entity.player;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.naxxsoftwares.mod.Initializer;
import org.jetbrains.annotations.NotNull;

public abstract class PlayerUtils {

    private static final MinecraftClient client = Initializer.client;

    public static boolean isInReach(@NotNull Entity entity) {
        boolean canReachFeet = client.player.getEyePos().squaredDistanceTo(entity.getPos()) <= MathHelper.square(client.player.getEntityInteractionRange());
        boolean canReachMid = client.player.getEyePos().squaredDistanceTo(entity.getPos().offset(Direction.UP, entity.getBoundingBox().getLengthY() / 2)) <= MathHelper.square(client.player.getEntityInteractionRange());
        boolean canReachHead = client.player.getEyePos().squaredDistanceTo(entity.getPos().offset(Direction.UP, entity.getBoundingBox().getLengthY())) <= MathHelper.square(client.player.getEntityInteractionRange());
        return canReachHead || canReachMid || canReachFeet;
    }
}
