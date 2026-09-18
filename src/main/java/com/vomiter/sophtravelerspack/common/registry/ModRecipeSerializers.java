package com.vomiter.sophtravelerspack.common.registry;

import com.vomiter.sophtravelerspack.STBackpack;
import com.vomiter.sophtravelerspack.common.recipe.ApplyTravelerTypeRecipe;
import com.vomiter.sophtravelerspack.common.recipe.RemoveTravelerTypeRecipe;
import com.vomiter.sophtravelerspack.common.recipe.TravelerTypeRecipeSerializer;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>>
            RECIPE_SERIALIZERS =
            DeferredRegister.create(
                    ForgeRegistries.RECIPE_SERIALIZERS,
                    STBackpack.MODID
            );

    public static final RegistryObject<RecipeSerializer<?>>
            APPLY_TRAVELER_TYPE =
            RECIPE_SERIALIZERS.register(
                    "apply_traveler_type",
                    () -> new TravelerTypeRecipeSerializer<>(
                            ApplyTravelerTypeRecipe::new
                    )
            );

    public static final RegistryObject<RecipeSerializer<?>>
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