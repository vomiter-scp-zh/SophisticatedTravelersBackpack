package com.vomiter.sophtravelerspack.mixin;

import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackFluidHandler;
import net.p3pp3rf1y.sophisticatedcore.upgrades.tank.TankUpgradeWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(value = BackpackFluidHandler.class, remap = false)
public interface SophBackpackFluidHandlerAccessor {
    @Invoker("getAllTanks")
    List<TankUpgradeWrapper> stbp$getAllTanks();
}
