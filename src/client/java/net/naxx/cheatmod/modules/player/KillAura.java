package net.naxx.cheatmod.modules.player;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.naxx.cheatmod.modules.Module;
import net.naxx.cheatmod.utils.entity.player.PlayerUtils;

import java.util.HashMap;
import java.util.List;

public final class KillAura extends Module {
    private static final String description = "Make them sway beneath your feet";
    private static final HashMap<String, Float> settings = new HashMap<>();
    private static final RunCategory runCategory = RunCategory.onEndingTick;

    public KillAura() {
        super(description, runCategory);
        settings.put("radius", 5F); //Default value
    }

    @Override
    public void onWorldJoin(ClientWorld world) {

    }

    @Override
    public void onActivate() {
    }

    @Override
    public void onDeactivate() {
    }

    @Override
    public void run() {
        List<AbstractClientPlayerEntity> nearbyPlayers = client.player.clientWorld.getPlayers().stream().filter(this::shouldTarget).toList();

        for (PlayerEntity target : nearbyPlayers) {
            this.silentlyRotateHeadOn(target);
            if (!shouldHit(target)) continue;
            client.interactionManager.attackEntity(client.player, target);
            client.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }

    }

    private boolean shouldTarget(PlayerEntity target) {
        ClientPlayerEntity player = client.player;
        return player != target && player.isInRange(target, settings.get("radius")) && PlayerUtils.getGamemode(target).isSurvivalLike();
    }

    //See Entity#lookAt
    private void silentlyRotateHeadOn(PlayerEntity target) {
        ClientPlayerEntity player = client.player;
        Vec3d targetPos = target.getPos();
        Vec3d playerEyePos = player.getEyePos();
        float yaw;
        float pitch;

        Vec3d targetMaxY = targetPos.offset(Direction.UP, target.getBoundingBox().getLengthY());
        Vec3d targetMidY = targetPos.offset(Direction.UP, target.getBoundingBox().getLengthY() / 2);

        double squaredDistanceToMaxY = playerEyePos.squaredDistanceTo(targetMaxY);
        double squaredDistanceToMidY = playerEyePos.squaredDistanceTo(targetMidY);
        double squaredDistanceToMinY = playerEyePos.squaredDistanceTo(targetPos); //Feet position ?

        double shortestSquaredDistanceToTarget = Math.min(Math.min(squaredDistanceToMaxY, squaredDistanceToMidY), squaredDistanceToMinY);

        if (shortestSquaredDistanceToTarget == squaredDistanceToMaxY) targetPos = targetMaxY;
        else if (shortestSquaredDistanceToTarget == squaredDistanceToMidY) targetPos = targetMidY;

        double d = targetPos.x - playerEyePos.x;
        double e = targetPos.y - playerEyePos.y;
        double f = targetPos.z - playerEyePos.z;
        double g = Math.sqrt(d * d + f * f);

        yaw = MathHelper.wrapDegrees((float) (MathHelper.atan2(f, d) * 180.0F / (float) Math.PI) - 90.0F);
        pitch = MathHelper.wrapDegrees((float) (-(MathHelper.atan2(e, g) * 180.0F / (float) Math.PI)));

        client.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, player.isOnGround()));
    }

    private boolean shouldHit(PlayerEntity target) {
        ClientPlayerEntity player = client.player;
        return player.getAttackCooldownProgress(1F / 20F) == 1F && player.squaredDistanceTo(target) <= MathHelper.square(player.getEntityInteractionRange() - Math.random());
    }
}
