package net.naxxsoftwares.mod.modules.combat;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
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

    public @Nullable PlayerEntity getTarget() {
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

    private @Nullable PlayerEntity setTarget(@NotNull ClientWorld world) {
        Vec3d playerEyePos = client.player.getEyePos();
        return getAllValidPlayers(world).stream().min(Comparator.comparingDouble(player -> playerEyePos.squaredDistanceTo(player.getPos()))).orElse(null);
    }

    private @NotNull List<AbstractClientPlayerEntity> getAllValidPlayers(@NotNull ClientWorld world) {
        return world.getPlayers().stream().filter(this::isValidTarget).toList();
    }

    private boolean isValidTarget(@NotNull PlayerEntity target) {
        return target != client.player && GamemodeUtils.getGamemode(target).isSurvivalLike() && PlayerUtils.isEntityInReach(target, SETTINGS.get("targetRadius"));
    }

    private boolean canHit(@NotNull PlayerEntity player) {
        return PlayerUtils.isCooldownFinished() && PlayerUtils.isEntityInReach(player, SETTINGS.get("hitRadius"));
    }

    @SuppressWarnings("DataFlowIssue")
    private void attack(PlayerEntity target) {
        client.interactionManager.attackEntity(client.player, target);
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
