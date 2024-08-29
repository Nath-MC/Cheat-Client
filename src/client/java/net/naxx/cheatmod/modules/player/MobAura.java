package net.naxx.cheatmod.modules.player;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.naxx.cheatmod.modules.Module;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public final class MobAura extends Module {
    private static final String description = "Same but on hostiles mobs";
    private static final HashMap<String, Float> settings = new HashMap<>();
    private static final RunCategory runCategory = RunCategory.onEndingTick;
    public static MobEntity target;
    public static float serverYaw;
    public static float serverPitch;

    public MobAura() {
        super(description, runCategory);
        settings.put("targetRadius", 3F);
        settings.put("hitRadius", 3F);
    }

    @Override
    public void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof PlayerMoveC2SPacket.Full fullPacket && fullPacket.changesLook() && this.targetFound()) {
            Vec3d pos = new Vec3d(fullPacket.getX(clientPlayer.getX()), fullPacket.getY(clientPlayer.getY()), fullPacket.getZ(clientPlayer.getZ()));

            ci.cancel();
            network.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(pos.x, pos.y, pos.z, clientPlayer.isOnGround()));
            return;
        }

        if (packet instanceof PlayerMoveC2SPacket.LookAndOnGround lookPacket && this.targetFound())
            if (lookPacket.getYaw(0F) != serverYaw || lookPacket.getPitch(0F) != serverPitch) ci.cancel();
    }

    @Override
    public void run() {
        target = this.setTarget(clientWorld);
        if (this.targetFound()) {
            this.silentlyRotateHeadOn(target);
            if (this.canHit(target)) this.attack(target);
        }

    }

    private @Nullable MobEntity setTarget(ClientWorld world) {
        Iterable<Entity> entities = this.getAllLoadedEntities(world);
        List<MobEntity> mobs = new ArrayList<>(this.getMobEntitiesIn(entities).stream().toList());

        //Sort the mob list according to their distance to the player
        mobs.sort((player1, player2) -> {
            Vec3d playerEyePos = clientPlayer.getEyePos();
            double d1 = playerEyePos.squaredDistanceTo(player1.getPos());
            double d2 = playerEyePos.squaredDistanceTo(player2.getPos());
            return Double.compare(d1, d2);
        });

        for (MobEntity mob : mobs) {
            if (!this.isValid(mob)) continue;
            if (mob instanceof PassiveEntity) continue;
            if (mob.isTarget(clientPlayer, TargetPredicate.DEFAULT) || mob instanceof HostileEntity) return mob;
        }

        return null;
    }

    private Iterable<Entity> getAllLoadedEntities(@NotNull ClientWorld world) {
        return world.getEntities();
    }

    private @NotNull List<MobEntity> getMobEntitiesIn(@NotNull Iterable<Entity> entities) {
        List<MobEntity> mobs = new ArrayList<>();
        for (Entity entity : entities)
            if (entity instanceof MobEntity mob) mobs.add(mob);
        return mobs;
    }

    private boolean isValid(MobEntity target) {
        return clientPlayer.isInRange(target, settings.get("targetRadius"));
    }

    private boolean canHit(MobEntity player) {
        return clientPlayer.getAttackCooldownProgress(1F / 20F) == 1F && clientPlayer.squaredDistanceTo(player) <= MathHelper.square(settings.get("hitRadius") - Math.random());
    }

    private boolean targetFound() {
        return !Objects.isNull(target);
    }

    private void attack(MobEntity target) {
        this.silentlyRotateHeadOn(target);
        client.interactionManager.attackEntity(clientPlayer, target);
        network.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
    }

    //See Entity#lookAt
    private void silentlyRotateHeadOn(@NotNull MobEntity target) {
        Vec3d targetPos = target.getPos();
        Vec3d playerEyePos = clientPlayer.getEyePos();

        Vec3d targetMaxY = targetPos.offset(Direction.UP, target.getBoundingBox().getLengthY());
        Vec3d targetMidY = targetPos.offset(Direction.UP, target.getBoundingBox().getLengthY() / 2);

        double squaredDistanceToMaxY = playerEyePos.squaredDistanceTo(targetMaxY);
        double squaredDistanceToMidY = playerEyePos.squaredDistanceTo(targetMidY);
        double squaredDistanceToMinY = playerEyePos.squaredDistanceTo(targetPos); //Feet position

        double shortestSquaredDistanceToTarget = Math.min(Math.min(squaredDistanceToMaxY, squaredDistanceToMidY), squaredDistanceToMinY);

        if (shortestSquaredDistanceToTarget == squaredDistanceToMaxY) targetPos = targetMaxY;
        else if (shortestSquaredDistanceToTarget == squaredDistanceToMidY) targetPos = targetMidY;

        double d = targetPos.x - playerEyePos.x;
        double e = targetPos.y - playerEyePos.y;
        double f = targetPos.z - playerEyePos.z;
        double g = Math.sqrt(d * d + f * f);

        serverYaw = MathHelper.wrapDegrees((float) (MathHelper.atan2(f, d) * 180.0F / (float) Math.PI) - 90.0F);
        serverPitch = MathHelper.wrapDegrees((float) (-(MathHelper.atan2(e, g) * 180.0F / (float) Math.PI)));

        network.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(serverYaw, serverPitch, clientPlayer.isOnGround()));
    }
}
