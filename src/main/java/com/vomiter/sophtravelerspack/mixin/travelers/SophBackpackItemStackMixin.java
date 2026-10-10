package com.vomiter.sophtravelerspack.mixin.travelers;


import com.vomiter.sophtravelerspack.traveler.ITravelerDummyHolder;
import com.vomiter.sophtravelerspack.traveler.LegacyTravelerDataMigration;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ItemStack.class)
public class SophBackpackItemStackMixin implements ITravelerDummyHolder {
    @Inject(method = "parse(Lnet/minecraft/core/HolderLookup$Provider;Lnet/minecraft/nbt/Tag;)Ljava/util/Optional;",
            at = @At("RETURN"))
    private static void sophtravelerspack$migrateLegacyData(HolderLookup.Provider registries, Tag tag,
            CallbackInfoReturnable<Optional<ItemStack>> cir) {
        cir.getReturnValue().ifPresent(stack -> LegacyTravelerDataMigration.migrate(stack, registries));
    }

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
