package net.naxxsoftwares.mod.mixins;

import net.minecraft.entity.player.PlayerEntity;
import net.naxxsoftwares.mod.modules.Modules;
import net.naxxsoftwares.mod.modules.movement.Fly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Redirect(method = "getBlockBreakingSpeed", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isOnGround()Z"))
    private boolean spoofOnGround(PlayerEntity instance) {
        if (Modules.isModuleActive(Fly.class) && !instance.groundCollision) return true;
        return instance.isOnGround();
    }
}
