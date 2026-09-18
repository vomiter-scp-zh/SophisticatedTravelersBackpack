package com.vomiter.sophtravelerspack.common.registry;

import com.vomiter.sophtravelerspack.STBackpack;
import com.vomiter.sophtravelerspack.traveler.TravelerType;
import com.vomiter.sophtravelerspack.traveler.TravelerUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems;

public class ModTabs {
    public static DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(
            BuiltInRegistries.CREATIVE_MODE_TAB.key(),
            STBackpack.MODID
    );

    public static RegistryObject<CreativeModeTab> MOD_TAB = TABS.register("main", () -> CreativeModeTab.builder()
            .icon(() -> {
                var backpack = ModItems.BACKPACK.get().getDefaultInstance();
                TravelerUtil.set(backpack, ModTravelerTypeRegistry.get(TravelerType.WOLF));
                return backpack;
            })
            .title(Component.translatable("itemGroup.sophtravelerspack"))
            .displayItems((featureFlags, output) -> {
                for (TravelerType travelerType : TravelerType.values()) {
                    var backpack = ModItems.BACKPACK.get().getDefaultInstance();
                    TravelerUtil.set(backpack, ModTravelerTypeRegistry.get(travelerType));
                    output.accept(backpack);
                }
                com.vomiter.sophtravelerspack.common.registry.ModItems
                        .ITEMS.getEntries().forEach(item -> output.accept(item.get()));
            })
            .build());
}
