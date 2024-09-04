package net.naxxsoftwares.mod.modules.combat;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.naxxsoftwares.mod.modules.Module;
import net.naxxsoftwares.mod.utils.entity.player.PlayerUtils;
import net.naxxsoftwares.mod.utils.world.gamemode.GamemodeUtils;

public final class TriggerBot extends Module {

    public TriggerBot() {
        super("Automatically hit the targeted entity", RunType.onEndingTick);
    }

    @Override
    public void run() {
        if (client.crosshairTarget instanceof EntityHitResult targetedEntity && GamemodeUtils.isInSpectator()) {
            if (targetedEntity.getEntity() instanceof LivingEntity entity && PlayerUtils.isInReach(entity)) {
                client.interactionManager.attackEntity(client.player, entity);
            }
        }
    }
}
