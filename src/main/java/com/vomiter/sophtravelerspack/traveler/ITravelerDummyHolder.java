package com.vomiter.sophtravelerspack.traveler;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public interface ITravelerDummyHolder {
    ItemStack sophtravelerspack$getTraveler();
    void sophtravelerspack$setTraveler(ItemStack traveler);
    ItemContainerContents sophtravelerspack$getShadowSource();
    void sophtravelerspack$setShadowSource(ItemContainerContents source);
}
