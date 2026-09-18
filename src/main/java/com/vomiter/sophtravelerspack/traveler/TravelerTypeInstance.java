package com.vomiter.sophtravelerspack.traveler;

import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.function.Supplier;

public final class TravelerTypeInstance {
    private final String stringRepresentation;
    private final Supplier<ItemStack> travelerBackpackSupplier;

    public TravelerTypeInstance(
            String stringRepresentation,
            Supplier<ItemStack> travelerBackpackSupplier
    ) {
        this.stringRepresentation = Objects.requireNonNull(stringRepresentation);
        this.travelerBackpackSupplier = Objects.requireNonNull(travelerBackpackSupplier);
    }

    public String getStringRepresentation() {
        return stringRepresentation;
    }

    /**
     * 一律回傳獨立的 ItemStack，避免 registry entry 持有的 stack 被修改。
     */
    public ItemStack createTravelerBackpack() {
        ItemStack stack = Objects.requireNonNull(
                travelerBackpackSupplier.get(),
                "Traveler backpack supplier returned null"
        );

        return stack.copy();
    }

    @Override
    public String toString() {
        return stringRepresentation;
    }
}