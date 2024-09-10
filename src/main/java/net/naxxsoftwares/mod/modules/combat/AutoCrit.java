package net.naxxsoftwares.mod.modules.combat;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.naxxsoftwares.mod.events.Event;
import net.naxxsoftwares.mod.helper.TargetManager;
import net.naxxsoftwares.mod.mixininterfaces.IPlayerInteractEntityC2SPacket;
import net.naxxsoftwares.mod.modules.Module;
import net.naxxsoftwares.mod.modules.Modules;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public final class AutoCrit extends Module {

    public AutoCrit() {
        super("Performs a critical attack on each hit");
    }

    @Event
    public void onPacket(Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof IPlayerInteractEntityC2SPacket attackPacket && attackPacket.cheatClient$getType() == PlayerInteractEntityC2SPacket.InteractType.ATTACK) {
            boolean isSameTarget = true;
            for (Module module : Modules.getAllModulesMatching(Module::isActive))
                if (module instanceof TargetManager<?> targetManager && targetManager.getTarget() != attackPacket.cheatClient$getEntity()) isSameTarget = false;
            if (isSameTarget) {
                client.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(client.player.getX(), client.player.getY() + 0.01, client.player.getZ(), false));
                client.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(client.player.getX(), client.player.getY(), client.player.getZ(), false));
            }
        }
    }
}
