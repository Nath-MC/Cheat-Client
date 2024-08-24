package net.naxx.cheatmod.modules.movement;

import net.minecraft.client.world.ClientWorld;
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
    public void onWorldJoin(ClientWorld world) {

    }

    @Override
    public void onActivate() {
        if (client.player.getVelocity().y <= -1.3F && PlayerUtils.getOwnGamemode().isSurvivalLike()) {
            toggle();
            ChatUtils.sendClientMessage("You shouldn't activate §l%s§r while falling !", getName().getString());
        }
    }

    @Override
    public void onDeactivate() {
    }

    @Override
    public void run() {
        if (!client.player.isOnGround())
            client.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
    }

}
