package com.vomiter.sophtravelerspack.common.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TravelerTypeRecipeSerializer<T extends ShapelessRecipe> implements RecipeSerializer<T> {
    private final Factory<T> factory;
    private final MapCodec<T> codec;
    private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;

    public TravelerTypeRecipeSerializer(Factory<T> factory) {
        this.factory = factory;
        ShapelessRecipe.Serializer vanilla = new ShapelessRecipe.Serializer();
        codec = vanilla.codec().xmap(this::convert, recipe -> recipe);
        streamCodec = vanilla.streamCodec().map(this::convert, recipe -> recipe);
    }

    @Override
    public MapCodec<T> codec() {
        return codec;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
        return streamCodec;
    }

    private T convert(ShapelessRecipe recipe) {
        return factory.create(
                recipe.getGroup(),
                recipe.category(),
                recipe.getResultItem(RegistryAccess.EMPTY).copy(),
                recipe.getIngredients()
        );
    }

    @FunctionalInterface
    public interface Factory<T extends ShapelessRecipe> {
        T create(
                String group,
                CraftingBookCategory category,
                ItemStack result,
                NonNullList<Ingredient> ingredients
        );
    }
}
