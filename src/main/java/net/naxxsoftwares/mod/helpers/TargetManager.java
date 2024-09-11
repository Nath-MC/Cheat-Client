package net.naxxsoftwares.mod.helpers;

import net.minecraft.entity.Entity;

public interface TargetManager<T extends Entity> {

    boolean hasTarget();

    T getTarget();

    void clearTarget();
}
