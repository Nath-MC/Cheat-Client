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
import net.minecraft.util.math.Vec3d;
import net.naxxsoftwares.mod.events.Event;
import net.naxxsoftwares.mod.helper.TargetManager;
import net.naxxsoftwares.mod.modules.Module;
import net.naxxsoftwares.mod.utils.entity.player.PlayerUtils;
import net.naxxsoftwares.mod.utils.entity.player.RotationsUtils;
import net.naxxsoftwares.mod.utils.world.gamemode.GamemodeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class MobAura extends Module implements TargetManager<MobEntity> {

    private static final HashMap<String, Float> SETTINGS = new HashMap<>();
    public static @Nullable MobEntity target;
    public static float serverYaw;
    public static float serverPitch;

    public MobAura() {
        super("Same but on hostile mobs");
        SETTINGS.put("targetRadius", 4F);
        SETTINGS.put("hitRadius", 3F);
    }

    @Event
    public void onPacket(Packet<?> packet, @NotNull CallbackInfo event) {
        if (packet instanceof PlayerMoveC2SPacket.Full fullPacket && this.hasTarget()) {
            if (fullPacket.getYaw(0) != RotationsUtils.serverYaw || fullPacket.getPitch(0) != RotationsUtils.serverPitch) {
                Vec3d pos = new Vec3d(fullPacket.getX(0), fullPacket.getY(0), fullPacket.getZ(0));
                event.cancel();
                client.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(pos.x, pos.y, pos.z, client.player.isOnGround()));
            }
        } else if (packet instanceof PlayerMoveC2SPacket.LookAndOnGround lookPacket && this.hasTarget())
            if (lookPacket.getYaw(0F) != RotationsUtils.getServerYaw() || lookPacket.getPitch(0F) != RotationsUtils.getServerPitch()) event.cancel();
    }

    @Override
    public boolean hasTarget() {
        return target != null;
    }

    public @Nullable MobEntity getTarget() {
        return target;
    }

    @Override
    public void clearTarget() {
        target = null;
    }

    @SuppressWarnings("DataFlowIssue")
    @Event
    public void onTick(ClientWorld world) {
        if (GamemodeUtils.isInSpectator()) return;
        target = this.setTarget(world);
        if (this.hasTarget()) {
            this.rotateOn(target);
            if (this.canHit(target)) this.attack(target);
        }

    }

    private @Nullable MobEntity setTarget(@NotNull ClientWorld world) {
        Vec3d playerEyePos = client.player.getEyePos();
        return getAllValidMobs(world).stream().filter(this::isHostile).min(Comparator.comparingDouble(mob -> playerEyePos.squaredDistanceTo(mob.getPos()))).orElse(null);
    }

    private @NotNull List<MobEntity> getAllValidMobs(@NotNull ClientWorld world) {
        Stream<Entity> entityStream = StreamSupport.stream(world.getEntities().spliterator(), false);
        return entityStream.filter(entity -> entity instanceof MobEntity mob && isValidTarget(mob)).map(entity -> (MobEntity) entity).collect(Collectors.toList());
    }

    private boolean isValidTarget(@NotNull MobEntity mob) {
        return PlayerUtils.isEntityInReach(mob, SETTINGS.get("targetRadius"));
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
        return PlayerUtils.isCooldownFinished() && PlayerUtils.isEntityInReach(mob, SETTINGS.get("hitRadius"));
    }

    @SuppressWarnings("DataFlowIssue")
    private void attack(MobEntity target) {
        client.interactionManager.attackEntity(client.player, target);
        client.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
    }

    private void rotateOn(@NotNull MobEntity target) {
        RotationsUtils.setRotationOn(target);
        RotationsUtils.applyRotation(true);
    }

    @Override
    public void onActivate() {
        this.clearTarget();
    }
}
