package net.naxxsoftwares.mod.modules.combat;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.naxxsoftwares.mod.events.Event;
import net.naxxsoftwares.mod.helpers.TargetManager;
import net.naxxsoftwares.mod.mixins.ClientPlayerInteractionManagerAccessor;
import net.naxxsoftwares.mod.modules.Module;
import net.naxxsoftwares.mod.utils.GamemodeUtils;
import net.naxxsoftwares.mod.utils.PlayerUtils;
import net.naxxsoftwares.mod.utils.RotationsUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public final class KillAura extends Module implements TargetManager<PlayerEntity> {

    private static final HashMap<String, Float> SETTINGS = new HashMap<>();
    public static @Nullable PlayerEntity target;

    public KillAura() {
        super("Make them sway beneath your feet");
        SETTINGS.put("targetRadius", 5F);
        SETTINGS.put("hitRadius", 3F);
    }

    @Event
    public void onPacket(Packet<?> packet, @NotNull CallbackInfo event) {
        if (hasTarget() && packet instanceof PlayerMoveC2SPacket moveC2SPacket && moveC2SPacket.changesLook()) {
            if (moveC2SPacket instanceof PlayerMoveC2SPacket.Full fullPacket) {
                PlayerMoveC2SPacket.PositionAndOnGround newPacket = replacePacket(fullPacket);
                event.cancel();
                client.getNetworkHandler().sendPacket(newPacket);
                return;
            }

            PlayerMoveC2SPacket.LookAndOnGround lookPacket = (PlayerMoveC2SPacket.LookAndOnGround) moveC2SPacket;

            if (lookPacket.getYaw(Float.MAX_VALUE) == RotationsUtils.serverYaw) return;
            if (lookPacket.getPitch(Float.MAX_VALUE) == RotationsUtils.serverPitch) return;

            event.cancel();
        }
    }

    private PlayerMoveC2SPacket.PositionAndOnGround replacePacket(PlayerMoveC2SPacket.@NotNull Full packet) {
        return new PlayerMoveC2SPacket.PositionAndOnGround(packet.getX(Double.MAX_VALUE), packet.getY(Double.MAX_VALUE), packet.getZ(Double.MAX_VALUE), packet.isOnGround());
    }

    @Override
    public boolean hasTarget() {
        return target != null;
    }

    public @Nullable PlayerEntity getTarget() {
        return target;
    }

    @Override
    public void clearTarget() {
        target = null;
    }

    @SuppressWarnings("DataFlowIssue")
    @Event
    public void onEndingTick() {
        if (GamemodeUtils.isInSpectator()) return;
        target = this.setTarget(client.world);
        if (this.hasTarget()) {
            this.rotateOn(target);
            if (this.canHit(target)) this.attack(target);
        }
    }

    private @Nullable PlayerEntity setTarget(@NotNull ClientWorld world) {
        Vec3d playerEyePos = client.player.getEyePos();
        return getAllValidPlayers(world).stream().min(Comparator.comparingDouble(player -> playerEyePos.squaredDistanceTo(player.getPos()))).orElse(null);
    }

    private @NotNull List<AbstractClientPlayerEntity> getAllValidPlayers(@NotNull ClientWorld world) {
        return world.getPlayers().stream().filter(this::isValidTarget).toList();
    }

    private boolean isValidTarget(@NotNull PlayerEntity target) {
        return target != client.player && target.isAlive() && GamemodeUtils.getGamemode(target).isSurvivalLike() && PlayerUtils.fastIsEntityInReach(target, SETTINGS.get("targetRadius"));
    }

    private boolean canHit(@NotNull PlayerEntity player) {
        return PlayerUtils.isCooldownFinished() && PlayerUtils.isEntityInReach(player, SETTINGS.get("hitRadius"));
    }

    @SuppressWarnings("DataFlowIssue")
    private void attack(PlayerEntity target) {
        ((ClientPlayerInteractionManagerAccessor) client.interactionManager).invokeSyncSelectedSlot();
        client.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(target, client.player.isSneaking()));
        client.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
    }

    private void rotateOn(PlayerEntity target) {
        RotationsUtils.setRotationOn(target);
        RotationsUtils.applyRotation(true);
    }

    @Override
    public void onActivate() {
        this.clearTarget();
    }
}
