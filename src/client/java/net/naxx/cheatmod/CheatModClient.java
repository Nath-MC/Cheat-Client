package net.naxx.cheatmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.naxx.cheatmod.modules.ClickTP;
import net.naxx.cheatmod.modules.Fly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheatModClient implements ClientModInitializer {
    public static final String MOD_ID = "assets/cheatmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        Fly fly = Fly.getINSTANCE();
        ClickTP clickTP = ClickTP.getInstance();

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            clickTP.onTick();
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            fly.onTick();
        });

        LOGGER.info("Modules instantiated");
    }
}