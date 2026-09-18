package com.vomiter.sophtravelerspack.compat;

import com.tiviacz.travelersbackpack.init.ModItems;
import com.vomiter.sophtravelerspack.STBackpack;
import com.vomiter.sophtravelerspack.common.recipe.ApplyTravelerTypeRecipe;
import com.vomiter.sophtravelerspack.common.recipe.RemoveTravelerTypeRecipe;
import com.vomiter.sophtravelerspack.common.registry.ModTravelerTypeRegistry;
import com.vomiter.sophtravelerspack.traveler.TravelerTypeInstance;
import com.vomiter.sophtravelerspack.traveler.TravelerUtil;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class STBackpackJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = STBackpack.modLoc( "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        List<CraftingRecipe> applyRecipes = new ArrayList<>();
        List<CraftingRecipe> removeRecipes = new ArrayList<>();

        int index = 0;

        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            if (!(item instanceof BackpackItem)) {
                continue;
            }

            for (TravelerTypeInstance type :
                    ModTravelerTypeRegistry.REGISTRY.get().getValues()) {
                ItemStack sophisticated = item.getDefaultInstance();
                ItemStack specialTraveler = type.createTravelerBackpack();
                ItemStack standardTraveler =
                        ModItems.STANDARD_TRAVELERS_BACKPACK.get()
                                .getDefaultInstance();

                ItemStack typedSophisticated = sophisticated.copy();
                TravelerUtil.set(typedSophisticated, type);

                applyRecipes.add(new ApplyTravelerTypeRecipe(
                        STBackpack.modLoc("jei/apply_traveler_type_" + index),
                        "",
                        CraftingBookCategory.MISC,
                        typedSophisticated.copy(),
                        NonNullList.of(
                                Ingredient.EMPTY,
                                Ingredient.of(sophisticated),
                                Ingredient.of(specialTraveler)
                        )
                ));

                removeRecipes.add(new RemoveTravelerTypeRecipe(
                        STBackpack.modLoc("jei/remove_traveler_type_" + index),
                        "",
                        CraftingBookCategory.MISC,
                        sophisticated.copy(),
                        NonNullList.of(
                                Ingredient.EMPTY,
                                Ingredient.of(typedSophisticated),
                                Ingredient.of(standardTraveler)
                        )
                ));

                index++;
            }
        }

        registration.addRecipes(RecipeTypes.CRAFTING, applyRecipes);
        registration.addRecipes(RecipeTypes.CRAFTING, removeRecipes);
    }
}