package net.naxxsoftwares.mod.mixins;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.naxxsoftwares.mod.events.packet.PacketEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin {

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        PacketEvent.SEND.invoker().onPacketSend(packet, ci);
    }

}
