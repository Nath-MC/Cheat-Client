package net.naxx.cheatmod.utils.chat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;


public class ChatUtils {
    static final MinecraftClient INSTANCE = MinecraftClient.getInstance();
    static final String PREFIX = "§d§lCheatMod§r  §l|§r  ";

    public static void sendMessage(String message) {
        INSTANCE.inGameHud.getChatHud().addMessage(Text.of(PREFIX + message));
    }
}
