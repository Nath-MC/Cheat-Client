package net.naxxsoftwares.mod.mixins;

import net.minecraft.client.world.ClientWorld;
import net.naxxsoftwares.mod.events.tick.TickEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {

    @Inject(at = @At("TAIL"), method = "tickEntities")
    private void onEndTick(CallbackInfo ci) {
        TickEvents.WORLD_TICK.invoker().onWorldTick((ClientWorld) (Object) this);
    }
}
