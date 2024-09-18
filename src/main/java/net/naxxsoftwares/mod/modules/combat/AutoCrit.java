package net.naxxsoftwares.mod.modules.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.naxxsoftwares.mod.accessors.PlayerInteractEntityC2SPacketAccessor;
import net.naxxsoftwares.mod.events.Event;
import net.naxxsoftwares.mod.helpers.TargetManager;
import net.naxxsoftwares.mod.modules.Module;
import net.naxxsoftwares.mod.modules.Modules;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public final class AutoCrit extends Module {

    public AutoCrit() {
        super("Performs a critical attack on each hit");
    }

    @Event
    public void onPacket(Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof PlayerInteractEntityC2SPacketAccessor interactPacket && isAttackType(interactPacket) && canCrit() && client.player.fallDistance <= 0) {
            Entity hitEntity = interactPacket.cheatClient$getEntity();

            if (!(hitEntity instanceof LivingEntity)) return;

            boolean isSameTarget = true;

            for (Module module : Modules.getAllModulesMatching(module -> module.isActive() && module instanceof TargetManager<?>))
                if (((TargetManager<?>) module).getTarget() != interactPacket.cheatClient$getEntity()) isSameTarget = false;

            if (!isSameTarget) return;

            client.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(client.player.getX(), client.player.getY() + 0.000001, client.player.getZ(), false));
            client.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(client.player.getX(), client.player.getY(), client.player.getZ(), false));
        }
    }

    private boolean isAttackType(PlayerInteractEntityC2SPacketAccessor packet) {
        return packet.cheatClient$getType() == PlayerInteractEntityC2SPacket.InteractType.ATTACK;
    }

    private boolean canCrit() {
        return !client.player.isTouchingWater() && !client.player.isClimbing() && !client.player.hasStatusEffect(StatusEffects.BLINDNESS) && !client.player.hasVehicle() && client.player.getAttackCooldownProgress(0.5F) > 0.9F;
    }
}
