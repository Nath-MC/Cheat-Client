package net.naxxsoftwares.mod.mixins;

import net.minecraft.client.world.ClientWorld;
import net.naxxsoftwares.mod.events.tick.TickEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {

    @Inject(method = "tickEntities", at = @At("HEAD"))
    private void startWorldTick(CallbackInfo ci) {
        TickEvents.WORLD_STARTING_TICK.invoker().onWorldTick();
    }

    @Inject(method = "tickEntities", at = @At("TAIL"))
    public void tickWorldAfterBlockEntities(CallbackInfo ci) {
        TickEvents.WORLD_ENDING_TICK.invoker().onWorldTick();
    }
}
