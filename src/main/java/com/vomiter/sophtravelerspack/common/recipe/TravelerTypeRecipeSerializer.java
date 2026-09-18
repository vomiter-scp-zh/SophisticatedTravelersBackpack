package com.vomiter.sophtravelerspack.common.recipe;

import com.google.gson.JsonObject;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TravelerTypeRecipeSerializer<T extends ShapelessRecipe>
        extends ShapelessRecipe.Serializer {

    private final Factory<T> factory;

    public TravelerTypeRecipeSerializer(Factory<T> factory) {
        this.factory = factory;
    }

    @Override
    public T fromJson(ResourceLocation id, JsonObject json) {
        ShapelessRecipe vanillaRecipe = super.fromJson(id, json);

        return convert(id, vanillaRecipe);
    }

    @Override
    public T fromNetwork(
            ResourceLocation id,
            FriendlyByteBuf buffer
    ) {
        ShapelessRecipe vanillaRecipe =
                super.fromNetwork(id, buffer);

        return convert(id, vanillaRecipe);
    }

    private T convert(
            ResourceLocation id,
            ShapelessRecipe recipe
    ) {
        return factory.create(
                id,
                recipe.getGroup(),
                recipe.category(),
                ModItems.BACKPACK.get().getDefaultInstance(),
                recipe.getIngredients()
        );
    }

    @FunctionalInterface
    public interface Factory<T extends ShapelessRecipe> {
        T create(
                ResourceLocation id,
                String group,
                CraftingBookCategory category,
                ItemStack result,
                NonNullList<Ingredient> ingredients
        );
    }
}