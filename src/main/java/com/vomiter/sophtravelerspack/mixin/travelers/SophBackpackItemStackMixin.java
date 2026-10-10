package com.vomiter.sophtravelerspack.mixin.travelers;


import com.vomiter.sophtravelerspack.traveler.ITravelerDummyHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemStack.class)
public class SophBackpackItemStackMixin implements ITravelerDummyHolder {
    @Unique
    ItemStack sophtravelerspack$traveler;

    @Unique
    private ItemContainerContents sophtravelerspack$shadowSource;

    @Override
    public ItemStack sophtravelerspack$getTraveler() {
        return sophtravelerspack$traveler;
    }

    @Override
    public void sophtravelerspack$setTraveler(ItemStack traveler) {
        this.sophtravelerspack$traveler = traveler;
    }
    @Override
    public ItemContainerContents sophtravelerspack$getShadowSource() {
        return sophtravelerspack$shadowSource;
    }

    @Override
    public void sophtravelerspack$setShadowSource(ItemContainerContents source) {
        sophtravelerspack$shadowSource = source;
    }
}
