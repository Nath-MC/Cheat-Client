package net.naxxsoftwares.mod.modules.combat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.naxxsoftwares.mod.events.Event;
import net.naxxsoftwares.mod.modules.Module;
import net.naxxsoftwares.mod.utils.GamemodeUtils;
import net.naxxsoftwares.mod.utils.PlayerUtils;

public final class TriggerBot extends Module {

    public TriggerBot() {
        super("Automatically hit the targeted entity");
    }

    @Event
    public void onClientTick(MinecraftClient client) {
        if (client.crosshairTarget.getType() == HitResult.Type.ENTITY && !GamemodeUtils.isInSpectator()) {
            if (((EntityHitResult) client.crosshairTarget).getEntity() instanceof LivingEntity entity && PlayerUtils.isEntityInReach(entity) && PlayerUtils.isCooldownFinished()) {
                if (entity instanceof PlayerEntity player) {
                    if (GamemodeUtils.getGamemode(player).isSurvivalLike()) hit(player);
                } else hit(entity);
            }
        }
    }

    private void hit(LivingEntity entity) {
        client.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        client.interactionManager.attackEntity(client.player, entity);
    }
}
