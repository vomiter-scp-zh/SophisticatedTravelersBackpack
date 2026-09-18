package com.vomiter.sophtravelerspack.common.recipe;

import com.tiviacz.travelersbackpack.init.ModItems;
import com.tiviacz.travelersbackpack.items.TravelersBackpackItem;
import com.vomiter.sophtravelerspack.common.registry.ModRecipeSerializers;
import com.vomiter.sophtravelerspack.traveler.TravelerTypeInstance;
import com.vomiter.sophtravelerspack.traveler.TravelerUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class RemoveTravelerTypeRecipe extends ShapelessRecipe {
    public RemoveTravelerTypeRecipe(
            ResourceLocation id,
            String group,
            CraftingBookCategory category,
            ItemStack result,
            NonNullList<Ingredient> ingredients
    ) {
        super(id, group, category, result, ingredients);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        ItemStack sophisticatedBackpack = ItemStack.EMPTY;
        ItemStack standardTravelerBackpack = ItemStack.EMPTY;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);

            if (stack.isEmpty()) {
                continue;
            }

            if (stack.getItem() instanceof BackpackItem) {
                if (!sophisticatedBackpack.isEmpty()) {
                    return false;
                }

                if (TravelerUtil.get(stack).isEmpty()) {
                    return false;
                }

                sophisticatedBackpack = stack;
                continue;
            }

            if (stack.getItem() instanceof TravelersBackpackItem) {
                if (!standardTravelerBackpack.isEmpty()) {
                    return false;
                }

                if (!stack.is(ModItems.STANDARD_TRAVELERS_BACKPACK.get())) {
                    return false;
                }

                standardTravelerBackpack = stack;
                continue;
            }

            return false;
        }

        return !sophisticatedBackpack.isEmpty()
                && !standardTravelerBackpack.isEmpty();
    }

    @Override
    public ItemStack assemble(
            CraftingContainer container,
            RegistryAccess registryAccess
    ) {
        ItemStack sophisticatedBackpack = ItemStack.EMPTY;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);

            if (stack.getItem() instanceof BackpackItem) {
                sophisticatedBackpack = stack;
                break;
            }
        }

        if (sophisticatedBackpack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        TravelerTypeInstance travelerType = TravelerUtil.get(sophisticatedBackpack).orElse(null);

        if (travelerType == null) {
            return ItemStack.EMPTY;
        }

        ItemStack result = sophisticatedBackpack.copy();
        result.setCount(1);

        TravelerUtil.clear(result);

        return result;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(
            CraftingContainer container
    ) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(
                container.getContainerSize(),
                ItemStack.EMPTY
        );

        TravelerTypeInstance travelerType = null;
        int standardBackpackSlot = -1;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);

            if (stack.isEmpty()) {
                continue;
            }

            if (stack.getItem() instanceof BackpackItem) {
                travelerType = TravelerUtil.get(stack).orElse(null);
                continue;
            }

            if (stack.is(ModItems.STANDARD_TRAVELERS_BACKPACK.get())) {
                standardBackpackSlot = i;
            }
        }

        if (travelerType == null || standardBackpackSlot < 0) {
            return remaining;
        }

        ItemStack standardBackpack =
                container.getItem(standardBackpackSlot);

        remaining.set(
                standardBackpackSlot,
                TravelerUtil.changeBackpackItem(
                        standardBackpack,
                        travelerType.createTravelerBackpack().getItem()
                )
        );

        return remaining;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.REMOVE_TRAVELER_TYPE.get();
    }
}