package com.vomiter.sophtravelerspack.traveler;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.function.Supplier;

public final class TravelerTypeInstance {
    private final String stringRepresentation;
    private final Supplier<ItemStack> travelerBackpackSupplier;
    private final ResourceLocation textureId;

    public TravelerTypeInstance(
            String stringRepresentation,
            Supplier<ItemStack> travelerBackpackSupplier,
            ResourceLocation textureId
    ) {
        this.stringRepresentation = Objects.requireNonNull(stringRepresentation);
        this.travelerBackpackSupplier = Objects.requireNonNull(travelerBackpackSupplier);
        this.textureId = textureId;
    }

    public String getStringRepresentation() {
        return stringRepresentation;
    }

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

    public ResourceLocation getTextureId() {
        return textureId;
    }
}