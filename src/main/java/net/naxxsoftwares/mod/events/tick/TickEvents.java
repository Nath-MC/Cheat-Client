package net.naxxsoftwares.mod.events.tick;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public abstract class TickEvents {

    public static final Event<ClientTick> CLIENT_STARTING_TICK = EventFactory.createArrayBacked(ClientTick.class, callbacks -> () -> {
        for (ClientTick event : callbacks)
            event.onClientTick();
    });

    public static final Event<WorldTick> WORLD_STARTING_TICK = EventFactory.createArrayBacked(WorldTick.class, callbacks -> () -> {
        for (WorldTick event : callbacks)
            event.onWorldTick();
    });

    public static final Event<WorldTick> WORLD_ENDING_TICK = EventFactory.createArrayBacked(WorldTick.class, callbacks -> () -> {
        for (WorldTick event : callbacks)
            event.onWorldTick();
    });

    @FunctionalInterface
    public interface ClientTick {
        void onClientTick();
    }

    @FunctionalInterface
    public interface WorldTick {
        void onWorldTick();
    }
}
