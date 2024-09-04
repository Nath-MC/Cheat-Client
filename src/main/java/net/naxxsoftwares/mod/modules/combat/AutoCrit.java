package net.naxxsoftwares.mod.modules.combat;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.naxxsoftwares.mod.mixininterfaces.IPlayerInteractEntityC2SPacket;
import net.naxxsoftwares.mod.modules.Module;
import net.naxxsoftwares.mod.modules.Modules;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public final class AutoCrit extends Module {

    public AutoCrit() {
        super("Performs a critical attack on each hit", RunType.onEndingTick);
    }

    @Override
    public void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof IPlayerInteractEntityC2SPacket attackPacket && attackPacket.getType() == PlayerInteractEntityC2SPacket.InteractType.ATTACK) {
            if (Modules.isModuleActive(KillAura.class) || Modules.isModuleActive(MobAura.class))
                if (KillAura.getTarget() != attackPacket.getEntity() && MobAura.getTarget() != attackPacket.getEntity()) return;
            client.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(client.player.getX(), client.player.getY() + 0.01, client.player.getZ(), false));
            client.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(client.player.getX(), client.player.getY(), client.player.getZ(), false));
        }
    }

    @Override
    public void run() {}
}
