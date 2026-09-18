package com.vomiter.sophtravelerspack.common.recipe;

import com.tiviacz.travelersbackpack.init.ModItems;
import com.tiviacz.travelersbackpack.items.TravelersBackpackItem;
import com.vomiter.sophtravelerspack.common.registry.ModRecipeSerializers;
import com.vomiter.sophtravelerspack.common.registry.ModTravelerTypeRegistry;
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
public class ApplyTravelerTypeRecipe extends ShapelessRecipe {
    public ApplyTravelerTypeRecipe(
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
        TravelerTypeInstance travelerType = null;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);

            if (stack.isEmpty()) {
                continue;
            }

            if (stack.getItem() instanceof BackpackItem) {
                if (!sophisticatedBackpack.isEmpty()) {
                    return false;
                }

                // Apply 只能用在尚未具有 Traveler Type 的 SB。
                if (TravelerUtil.get(stack).isPresent()) {
                    return false;
                }

                sophisticatedBackpack = stack;
                continue;
            }

            if (stack.getItem() instanceof TravelersBackpackItem) {
                // Standard backpack 不能拿來 Apply。
                if (stack.is(ModItems.STANDARD_TRAVELERS_BACKPACK.get())) {
                    return false;
                }

                if (travelerType != null) {
                    return false;
                }

                travelerType = ModTravelerTypeRegistry.REGISTRY.get().getValues().stream().filter(
                        travelerTypeInstance -> ItemStack.isSameItem(travelerTypeInstance.createTravelerBackpack(), stack)
                ).findFirst().orElse(null);

                if (travelerType == null) {
                    return false;
                }

                continue;
            }

            // 不允許額外 ingredient。
            return false;
        }

        return !sophisticatedBackpack.isEmpty()
                && travelerType != null;
    }

    @Override
    public ItemStack assemble(
            CraftingContainer container,
            RegistryAccess registryAccess
    ) {
        ItemStack sophisticatedBackpack = ItemStack.EMPTY;
        TravelerTypeInstance travelerType = null;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);

            if (stack.isEmpty()) {
                continue;
            }

            if (stack.getItem() instanceof BackpackItem) {
                sophisticatedBackpack = stack;
            } else if (stack.getItem() instanceof TravelersBackpackItem
                    && !stack.is(ModItems.STANDARD_TRAVELERS_BACKPACK.get())) {
                travelerType = ModTravelerTypeRegistry.REGISTRY.get().getValues().stream().filter(
                        travelerTypeInstance -> ItemStack.isSameItem(travelerTypeInstance.createTravelerBackpack(), stack)
                ).findFirst().orElse(null);
            }
        }

        if (sophisticatedBackpack.isEmpty() || travelerType == null) {
            return ItemStack.EMPTY;
        }

        ItemStack result = sophisticatedBackpack.copy();
        result.setCount(1);

        TravelerUtil.set(result, travelerType);

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

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);

            if (!(stack.getItem() instanceof TravelersBackpackItem)) {
                continue;
            }

            if (stack.is(ModItems.STANDARD_TRAVELERS_BACKPACK.get())) {
                continue;
            }

            TravelerTypeInstance type= ModTravelerTypeRegistry.REGISTRY.get().getValues().stream().filter(
                    travelerTypeInstance -> ItemStack.isSameItem(travelerTypeInstance.createTravelerBackpack(), stack)
            ).findFirst().orElse(null);


            if (type == null) {
                continue;
            }

            remaining.set(
                    i,
                    TravelerUtil.changeBackpackItem(
                            stack,
                            ModItems.STANDARD_TRAVELERS_BACKPACK.get()
                    )
            );
        }

        return remaining;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.APPLY_TRAVELER_TYPE.get();
    }
}