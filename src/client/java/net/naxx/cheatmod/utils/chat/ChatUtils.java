package net.naxx.cheatmod.utils.chat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.naxx.cheatmod.Initializer;


public abstract class ChatUtils {
    private static final MinecraftClient client = Initializer.client;
    private static final String PREFIX = "§d§lCheatMod§r  §l|§r  ";

    public static void sendClientMessage(String message, Object... args) {
        client.inGameHud.getChatHud().addMessage(Text.of(PREFIX + String.format(message, args)));
    }
}
