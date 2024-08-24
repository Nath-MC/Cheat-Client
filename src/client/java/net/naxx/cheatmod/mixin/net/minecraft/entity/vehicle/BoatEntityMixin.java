package net.naxx.cheatmod.mixin.net.minecraft.entity.vehicle;

import net.minecraft.entity.vehicle.BoatEntity;
import net.naxx.cheatmod.modules.Module;
import net.naxx.cheatmod.modules.movement.Fly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BoatEntity.class)
public abstract class BoatEntityMixin {

    @Shadow
    private boolean pressingRight;

    @Shadow
    private boolean pressingLeft;

    @Redirect(method = "updatePaddles", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/vehicle/BoatEntity;pressingRight:Z"))
    private boolean updatePaddlesR(BoatEntity instance) {
        if (Module.isActive(Fly.class)) return false;
        return pressingRight;
    }

    @Redirect(method = "updatePaddles", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/vehicle/BoatEntity;pressingLeft:Z"))
    private boolean updatePaddlesL(BoatEntity instance) {
        if (Module.isActive(Fly.class)) return false;
        return pressingLeft;
    }
}
