package net.naxxsoftwares.mod.modules.movement;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.naxxsoftwares.mod.events.Event;
import net.naxxsoftwares.mod.modules.Module;
import net.naxxsoftwares.mod.utils.ChatUtils;
import net.naxxsoftwares.mod.utils.GamemodeUtils;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public final class NoFall extends Module {

    public NoFall() {
        super("Cancel fall damage");
    }

    @Event
    public void onPacket(Packet<?> packet, @NotNull CallbackInfo ci) {
        if (packet instanceof PlayerMoveC2SPacket movePacket) {
            if (!movePacket.isOnGround() && !(client.player.verticalCollision && client.player.groundCollision) && client.player.fallDistance >= 3 && GamemodeUtils.getOwnGamemode().isSurvivalLike()) {
                ci.cancel();
                PlayerMoveC2SPacket newPacket = null;

                if (movePacket instanceof PlayerMoveC2SPacket.Full fullPacket) newPacket = changesOnGround(fullPacket);
                if (movePacket instanceof PlayerMoveC2SPacket.PositionAndOnGround posPacket) newPacket = changesOnGround(posPacket);
                if (movePacket instanceof PlayerMoveC2SPacket.LookAndOnGround lookPacket) newPacket = changesOnGround(lookPacket);
                if (movePacket instanceof PlayerMoveC2SPacket.OnGroundOnly) newPacket = changesOnGround();

                client.getNetworkHandler().sendPacket(newPacket);
            }
        }
    }

    private PlayerMoveC2SPacket.Full changesOnGround(PlayerMoveC2SPacket.@NotNull Full packet) {
        return new PlayerMoveC2SPacket.Full(packet.getX(Double.MAX_VALUE), packet.getY(Double.MAX_VALUE), packet.getZ(Double.MAX_VALUE), packet.getYaw(Float.MAX_VALUE), packet.getPitch(Float.MAX_VALUE), true);
    }

    private PlayerMoveC2SPacket.PositionAndOnGround changesOnGround(PlayerMoveC2SPacket.@NotNull PositionAndOnGround packet) {
        return new PlayerMoveC2SPacket.PositionAndOnGround(packet.getX(Double.MAX_VALUE), packet.getY(Double.MAX_VALUE), packet.getZ(Double.MAX_VALUE), true);
    }

    private PlayerMoveC2SPacket.LookAndOnGround changesOnGround(PlayerMoveC2SPacket.@NotNull LookAndOnGround packet) {
        return new PlayerMoveC2SPacket.LookAndOnGround(packet.getYaw(Float.MAX_VALUE), packet.getPitch(Float.MAX_VALUE), true);
    }

    private PlayerMoveC2SPacket.OnGroundOnly changesOnGround() {
        return new PlayerMoveC2SPacket.OnGroundOnly(true);
    }

    @Override
    public void onActivate() {
        if (client.player.getVelocity().y <= -1.3F && GamemodeUtils.getOwnGamemode().isSurvivalLike()) {
            this.toggle(); //Turn NoFall back off
            ChatUtils.sendClientMessage("You shouldn't activate §l%s§r while falling !", this.getName().getString());
        }
    }

}
