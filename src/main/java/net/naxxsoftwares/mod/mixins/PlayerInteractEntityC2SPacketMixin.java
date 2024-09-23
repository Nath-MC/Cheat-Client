package net.naxxsoftwares.mod.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import static net.naxxsoftwares.mod.Initializer.client;

@Mixin(PlayerInteractEntityC2SPacket.class)
public abstract class PlayerInteractEntityC2SPacketMixin implements PlayerInteractEntityC2SPacketAccessor {
    @Shadow
    @Final
    private PlayerInteractEntityC2SPacket.InteractTypeHandler type;
    @Shadow
    @Final
    private int entityId;

    @Override
    public PlayerInteractEntityC2SPacket.InteractType cheatClient$getType() {
        return type.getType();
    }

    @Override
    public Entity cheatClient$getEntity() {
        return client.world.getEntityById(entityId);
    }
}
