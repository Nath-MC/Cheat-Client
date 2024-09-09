package net.naxxsoftwares.mod.mixins;

import net.minecraft.client.MinecraftClient;
import net.naxxsoftwares.mod.events.tick.TickEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Inject(at = @At("HEAD"), method = "tick")
    private void onClientTick(CallbackInfo ci) {
        TickEvents.CLIENT_TICK.invoker().onClientTick((MinecraftClient) (Object) this);
    }

}
