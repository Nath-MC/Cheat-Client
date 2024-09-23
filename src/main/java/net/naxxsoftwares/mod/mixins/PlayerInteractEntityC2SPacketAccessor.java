package net.naxxsoftwares.mod.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;

public interface PlayerInteractEntityC2SPacketAccessor {
    PlayerInteractEntityC2SPacket.InteractType cheatClient$getType();

    Entity cheatClient$getEntity();
}
