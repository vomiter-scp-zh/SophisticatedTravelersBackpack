package com.vomiter.sophtravelerspack.common.registry;

import com.vomiter.sophtravelerspack.STBackpack;
import com.vomiter.sophtravelerspack.common.recipe.ApplyTravelerTypeRecipe;
import com.vomiter.sophtravelerspack.common.recipe.RemoveTravelerTypeRecipe;
import com.vomiter.sophtravelerspack.common.recipe.TravelerTypeRecipeSerializer;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>>
            RECIPE_SERIALIZERS =
            DeferredRegister.create(
                    Registries.RECIPE_SERIALIZER,
                    STBackpack.MODID
            );

    public static final DeferredHolder<RecipeSerializer<?>, TravelerTypeRecipeSerializer<ApplyTravelerTypeRecipe>>
            APPLY_TRAVELER_TYPE =
            RECIPE_SERIALIZERS.register(
                    "apply_traveler_type",
                    () -> new TravelerTypeRecipeSerializer<>(
                            ApplyTravelerTypeRecipe::new
                    )
            );

    public static final DeferredHolder<RecipeSerializer<?>, TravelerTypeRecipeSerializer<RemoveTravelerTypeRecipe>>
            REMOVE_TRAVELER_TYPE =
            RECIPE_SERIALIZERS.register(
                    "remove_traveler_type",
                    () -> new TravelerTypeRecipeSerializer<>(
                            RemoveTravelerTypeRecipe::new
                    )
            );

    public static void register(IEventBus modBus) {
        RECIPE_SERIALIZERS.register(modBus);
    }
}