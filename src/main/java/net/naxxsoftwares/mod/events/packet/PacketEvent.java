package net.naxxsoftwares.mod.events.packet;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public abstract class PacketEvent {

    public static final Event<Send> SEND = EventFactory.createArrayBacked(Send.class, callbacks -> (packet, event) -> {
        for (Send event1 : callbacks)
            event1.onPacketSend(packet, event);
    });

    @FunctionalInterface
    public interface Send {
        void onPacketSend(Packet<?> packet, CallbackInfo event);
    }
}
