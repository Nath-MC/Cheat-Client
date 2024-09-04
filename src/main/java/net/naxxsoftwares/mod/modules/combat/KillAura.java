package net.naxxsoftwares.mod.modules.combat;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.naxxsoftwares.mod.modules.Module;
import net.naxxsoftwares.mod.utils.world.gamemode.GamemodeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public final class KillAura extends Module {

    private static final HashMap<String, Float> SETTINGS = new HashMap<>();
    public static @Nullable PlayerEntity target;
    public static float serverYaw;
    public static float serverPitch;

    public KillAura() {
        super("Make them sway beneath your feet", RunType.onEndingTick);
        SETTINGS.put("targetRadius", 5F);
        SETTINGS.put("hitRadius", 3F);
    }

    @Override
    public void onSendPacket(Packet<?> packet, @NotNull CallbackInfo ci) {
        if (packet instanceof PlayerMoveC2SPacket.Full fullPacket && targetFound()) {
            Vec3d pos = new Vec3d(fullPacket.getX(client.player.getX()), fullPacket.getY(client.player.getY()), fullPacket.getZ(client.player.getZ()));
            ci.cancel();
            client.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(pos.x, pos.y, pos.z, client.player.isOnGround()));
        } else if (packet instanceof PlayerMoveC2SPacket.LookAndOnGround lookPacket && targetFound())
            if (lookPacket.getYaw(0F) != serverYaw || lookPacket.getPitch(0F) != serverPitch)
                ci.cancel();
    }

    @Override
    public void onActivate() {
        target = null;
        serverYaw = 0;
        serverPitch = 0;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void run() {
        if (GamemodeUtils.isInSpectator())
            return;
        target = this.findTarget(client.world);
        if (targetFound()) {
            this.rotateHeadToTarget(target);
            if (this.canHit(target))
                this.attack(target);
        }
    }

    private @Nullable PlayerEntity findTarget(@NotNull ClientWorld world) {
        Vec3d playerEyePos = client.player.getEyePos();
        return getAllValidPlayers(world).stream().min(Comparator.comparingDouble(player -> playerEyePos.squaredDistanceTo(player.getPos()))).orElse(null);
    }

    private @NotNull List<AbstractClientPlayerEntity> getAllValidPlayers(@NotNull ClientWorld world) {
        return world.getPlayers().stream().filter(this::isValidTarget).toList();
    }

    private boolean isValidTarget(@NotNull PlayerEntity target) {
        if (target == client.player)
            return false;
        if (!GamemodeUtils.getGamemode(target).isSurvivalLike())
            return false;
        if (client.player.getEyePos().squaredDistanceTo(target.getPos()) <= MathHelper.square(SETTINGS.get("targetRadius") * 1.5))
            return client.player.getEyePos().squaredDistanceTo(determineBestAimPoint(target)) <= MathHelper.square(SETTINGS.get("targetRadius"));
        return false;
    }

    private Vec3d determineBestAimPoint(@NotNull PlayerEntity target) {
        Vec3d playerEyePos = client.player.getEyePos();
        Vec3d targetPos = target.getPos();

        // Divide the mob's hitbox into 10 segments along the Y-axis
        double height = target.getBoundingBox().getLengthY();
        int numPoints = 18;
        Vec3d closestPoint = null;
        double closestDistance = Double.MAX_VALUE;

        for (int i = 0; i <= numPoints; i++) {
            double yOffset = (height / numPoints) * i;
            Vec3d point = targetPos.add(0, yOffset, 0);
            double distance = playerEyePos.squaredDistanceTo(point);

            if (distance < closestDistance) {
                closestDistance = distance;
                closestPoint = point;
            }
        }

        // Return the closest point found
        return closestPoint;
    }

    private boolean canHit(@NotNull PlayerEntity player) {
        return client.player.getAttackCooldownProgress(1F / 20F) == 1F && client.player.squaredDistanceTo(determineBestAimPoint(player)) <= MathHelper.square(SETTINGS.get("hitRadius") - Math.random());
    }

    @SuppressWarnings("DataFlowIssue")
    private void attack(PlayerEntity target) {
        client.interactionManager.attackEntity(client.player, target);
        client.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
    }

    //See Entity#lookAt
    private void rotateHeadToTarget(@NotNull PlayerEntity target) {
        Vec3d targetPos = determineBestAimPoint(target);
        Vec3d playerEyePos = client.player.getEyePos();

        double d = targetPos.x - playerEyePos.x;
        double e = targetPos.y - playerEyePos.y;
        double f = targetPos.z - playerEyePos.z;
        double g = Math.sqrt(d * d + f * f);

        serverYaw = MathHelper.wrapDegrees((float) (MathHelper.atan2(f, d) * 180.0F / Math.PI) - 90.0F);
        serverPitch = MathHelper.wrapDegrees((float) (-(MathHelper.atan2(e, g) * 180.0F / Math.PI)));

        client.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(serverYaw, serverPitch, client.player.isOnGround()));
    }

    public static boolean targetFound() {
        return !Objects.isNull(target);
    }

    public static @Nullable PlayerEntity getTarget() {
        return target;
    }
}
