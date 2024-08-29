package net.naxx.cheatmod.modules.movement;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.naxx.cheatmod.modules.Module;
import net.naxx.cheatmod.utils.chat.ChatUtils;
import net.naxx.cheatmod.utils.entity.player.PlayerUtils;

public final class NoFall extends Module {
    private static final String description = "Cancel fall damage";
    private final static RunCategory runCategory = RunCategory.onStartingTick;

    public NoFall() {
        super(description, runCategory);
    }

    @Override
    public void onActivate() {
        if (clientPlayer.getVelocity().y <= -1.3F && PlayerUtils.getOwnGamemode().isSurvivalLike()) {
            this.toggle(); //Turn NoFall back off
            ChatUtils.sendClientMessage("You shouldn't activate §l%s§r while falling !", this.getName().getString());
        }
    }

    @Override
    public void run() {
        if (!clientPlayer.isOnGround()) network.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
    }

}
