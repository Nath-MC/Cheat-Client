package net.naxx.cheatmod.mixin.net.minecraft.network;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.naxx.cheatmod.modules.Module;
import net.naxx.cheatmod.modules.Modules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin {
    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        for (Module module : new Modules())
            if (module.isActive()) module.onSendPacket(packet, ci);
    }
}
