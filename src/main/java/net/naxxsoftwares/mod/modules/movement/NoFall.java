package net.naxxsoftwares.mod.modules.movement;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.naxxsoftwares.mod.events.Event;
import net.naxxsoftwares.mod.modules.Module;
import net.naxxsoftwares.mod.utils.chat.ChatUtils;
import net.naxxsoftwares.mod.utils.world.gamemode.GamemodeUtils;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public final class NoFall extends Module {

    public NoFall() {
        super("Cancel fall damage");
    }

    @Event
    public void onPacket(Packet<?> packet, @NotNull CallbackInfo ci) {
        if (packet instanceof PlayerMoveC2SPacket movePacket) {
            if (!movePacket.isOnGround() && !(client.player.verticalCollision && client.player.groundCollision) && client.player.fallDistance > 3 && GamemodeUtils.getOwnGamemode().isSurvivalLike()) {
                ci.cancel();
                client.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(movePacket.getX(0), movePacket.getY(0), movePacket.getZ(0), movePacket.getYaw(client.player.getYaw()), movePacket.getPitch(0), true));
            }
        }
    }

    @Override
    public void onActivate() {
        if (client.player.getVelocity().y <= -1.3F && GamemodeUtils.getOwnGamemode().isSurvivalLike()) {
            this.toggle(); //Turn NoFall back off
            ChatUtils.sendClientMessage("You shouldn't activate §l%s§r while falling !", this.getName().getString());
        }
    }

}
