package net.naxx.cheatmod;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheatmodClient implements ClientModInitializer {
    public static final String MOD_ID = "cheatmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("Cheat Mod is launching");
    }
}