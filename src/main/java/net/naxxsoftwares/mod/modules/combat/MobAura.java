package net.naxxsoftwares.mod.modules.combat;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.naxxsoftwares.mod.events.Event;
import net.naxxsoftwares.mod.modules.Module;
import net.naxxsoftwares.mod.utils.world.gamemode.GamemodeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class MobAura extends Module {

    private static final HashMap<String, Float> SETTINGS = new HashMap<>();
    public static @Nullable MobEntity target;
    public static float serverYaw;
    public static float serverPitch;

    public MobAura() {
        super("Same but on hostile mobs");
        SETTINGS.put("targetRadius", 4F);
        SETTINGS.put("hitRadius", 3F);
    }

    public static @Nullable MobEntity getTarget() {
        return target;
    }

    @Event
    public void onPacket(Packet<?> packet, @NotNull CallbackInfo ci) {
        if (packet instanceof PlayerMoveC2SPacket.Full fullPacket && targetFound()) {
            Vec3d pos = new Vec3d(fullPacket.getX(client.player.getX()), fullPacket.getY(client.player.getY()), fullPacket.getZ(client.player.getZ()));
            ci.cancel();
            client.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(pos.x, pos.y, pos.z, client.player.isOnGround()));
        } else if (packet instanceof PlayerMoveC2SPacket.LookAndOnGround lookPacket && targetFound()) if (lookPacket.getYaw(0F) != serverYaw || lookPacket.getPitch(0F) != serverPitch) ci.cancel();
    }

    public static boolean targetFound() {
        return !Objects.isNull(target);
    }

    @Event
    public void onTick(ClientWorld world) {
        if (GamemodeUtils.isInSpectator()) return;
        target = this.findTarget(world);
        if (targetFound()) {
            this.rotateHeadToTarget(target);
            if (this.canHit(target)) this.attack(target);
        }

    }

    private @Nullable MobEntity findTarget(@NotNull ClientWorld world) {
        Vec3d playerEyePos = client.player.getEyePos();
        return getAllValidMobs(world).stream().filter(this::isHostile).min(Comparator.comparingDouble(mob -> playerEyePos.squaredDistanceTo(mob.getPos()))).orElse(null);
    }

    private @NotNull List<MobEntity> getAllValidMobs(@NotNull ClientWorld world) {
        Stream<Entity> entityStream = StreamSupport.stream(world.getEntities().spliterator(), false);

        return entityStream.filter(entity -> entity instanceof MobEntity mob && isNear(mob)).map(entity -> (MobEntity) entity).collect(Collectors.toList());
    }

    private boolean isNear(@NotNull MobEntity mob) {
        if (client.player.getEyePos().squaredDistanceTo(mob.getPos()) <= MathHelper.square(SETTINGS.get("targetRadius") * 1.5))
            return client.player.getEyePos().squaredDistanceTo(determineBestAimPoint(mob)) <= MathHelper.square(SETTINGS.get("targetRadius"));
        return false;
    }

    private @Nullable Vec3d determineBestAimPoint(@NotNull MobEntity target) {
        Vec3d playerEyePos = client.player.getEyePos();
        Vec3d targetPos = target.getPos();

        // Divide the mob's hitbox into 10 segments along the Y-axis
        double height = target.getBoundingBox().getLengthY();
        double offsets = 10;
        Vec3d closestPoint = null;
        double closestDistance = Double.MAX_VALUE;

        for (int i = 0; i <= offsets; i++) {
            double yOffset = (height / offsets) * i;
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

    private boolean isHostile(MobEntity mob) {
        if (!GamemodeUtils.getOwnGamemode().isSurvivalLike()) return false;

        boolean isTamedByPlayer = false;
        if (mob instanceof TameableEntity tameable) isTamedByPlayer = tameable.isTamed() && tameable.getOwnerUuid() == client.player.getUuid();

        if (mob instanceof EndermanEntity enderman) return enderman.isAttacking();

        if (mob instanceof SpiderEntity spider) return client.world.isNight() || spider.isAttacking();

        if (mob instanceof ZombifiedPiglinEntity piglin) return piglin.isAttacking();

        if (mob instanceof IronGolemEntity ironGolem) return ironGolem.isAttacking();

        if (mob instanceof WolfEntity wolf) return wolf.isAttacking() && !isTamedByPlayer;

        return mob instanceof HostileEntity;
    }

    private boolean canHit(@NotNull MobEntity mob) {
        return client.player.getAttackCooldownProgress(1F / 20F) == 1F && client.player.squaredDistanceTo(determineBestAimPoint(mob)) <= MathHelper.square(SETTINGS.get("hitRadius"));
    }

    private void attack(MobEntity target) {
        client.interactionManager.attackEntity(client.player, target);
        client.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
    }

    //See Entity#lookAt
    private void rotateHeadToTarget(@NotNull MobEntity target) {
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
}
