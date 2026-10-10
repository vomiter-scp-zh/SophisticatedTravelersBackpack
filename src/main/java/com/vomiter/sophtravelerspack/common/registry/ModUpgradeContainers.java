package com.vomiter.sophtravelerspack.common.registry;

import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.RegisterEvent;

public final class ModUpgradeContainers {

    public static void register(RegisterEvent event) {
        if (!event.getRegistryKey().equals(Registries.MENU)) {
            return;
        }
    }
}